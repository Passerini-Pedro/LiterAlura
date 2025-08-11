package com.alura.literalura.service;

import com.alura.literalura.dto.ApiBookResponse;
import com.alura.literalura.dto.GutenbergAuthor;
import com.alura.literalura.dto.GutenbergBook;
import com.alura.literalura.model.Autor;
import com.alura.literalura.model.Livro;
import com.alura.literalura.repository.AutorRepository;
import com.alura.literalura.repository.LivroRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GutenbergService {

    private static final String BASE_URL = "https://gutendex.com/books";

    private final RestTemplate rt;
    private final AutorRepository autorRepo;
    private final LivroRepository livroRepo;

    public GutenbergService(RestTemplate restTemplate,
                            AutorRepository autorRepo,
                            LivroRepository livroRepo) {
        this.rt = restTemplate;
        this.autorRepo = autorRepo;
        this.livroRepo = livroRepo;
    }

    /**
     * Busca um livro na API Gutendex por título (ou por ID/link) e persiste no banco.
     */
    public Optional<Livro> buscarELancar(String tituloBusca) {
        String url = UriComponentsBuilder
                .fromHttpUrl(BASE_URL)
                .queryParam("search", tituloBusca)
                .encode()
                .toUriString();

        System.out.println("Chamando API Gutenberg em: " + url);

        try {
            // tenta mapear diretamente para ApiBookResponse
            ApiBookResponse rawResponse = rt.getForObject(url, ApiBookResponse.class);

            // se não encontrou nada via search, tenta extrair ID e buscar /books/{id}
            if (rawResponse == null || rawResponse.getResults() == null || rawResponse.getResults().isEmpty()) {
                System.out.println("Nenhum resultado encontrado via 'search'. Tentando buscar por ID/link...");

                String id = extrairIdDoTituloOuLink(tituloBusca);
                if (id != null) {
                    String urlDireta = BASE_URL + "/" + id;
                    System.out.println("Tentando URL direta: " + urlDireta);
                    GutenbergBook bookDireto = rt.getForObject(urlDireta, GutenbergBook.class);
                    if (bookDireto != null) {
                        return salvarLivro(bookDireto);
                    } else {
                        System.out.println("Nenhum livro retornado na busca direta por ID.");
                        return Optional.empty();
                    }
                }

                // fallback: tentar fazer uma busca por palavra-chave menor (ex.: primeira palavra)
                String primeiraPalavra = extrairPrimeiraPalavra(tituloBusca);
                if (primeiraPalavra != null && !primeiraPalavra.isBlank()
                        && !primeiraPalavra.equalsIgnoreCase(tituloBusca)) {
                    System.out.println("Tentando busca por palavra-chave: " + primeiraPalavra);
                    String urlKeyword = UriComponentsBuilder
                            .fromHttpUrl(BASE_URL)
                            .queryParam("search", primeiraPalavra)
                            .encode()
                            .toUriString();
                    ApiBookResponse keywordResp = rt.getForObject(urlKeyword, ApiBookResponse.class);
                    if (keywordResp != null && keywordResp.getResults() != null && !keywordResp.getResults().isEmpty()) {
                        GutenbergBook book = keywordResp.getResults().get(0);
                        return salvarLivro(book);
                    }
                }

                return Optional.empty();
            }

            // se achou resultados, processa a lista
            List<GutenbergBook> results = rawResponse.getResults();
            String normalizedSearch = normalize(tituloBusca);

            GutenbergBook book = results.stream()
                    .filter(b -> b.getTitle() != null && normalize(b.getTitle()).contains(normalizedSearch))
                    .findFirst()
                    .orElse(results.get(0)); // fallback: primeiro resultado

            return salvarLivro(book);

        } catch (Exception e) {
            System.err.println("Erro ao acessar/processar Gutenberg: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Extrai um possível ID numérico de uma entrada (pode ser número puro ou link contendo número).
     */
    private String extrairIdDoTituloOuLink(String entrada) {
        if (entrada == null) return null;
        String trimmed = entrada.trim();
        // se for número puro, retorna
        if (trimmed.matches("\\d+")) return trimmed;
        // procura o primeiro número no texto (por exemplo, em uma URL)
        Pattern p = Pattern.compile("(\\d+)");
        Matcher m = p.matcher(trimmed);
        if (m.find()) return m.group(1);
        return null;
    }

    /**
     * Pega a primeira palavra de uma frase (útil para tentativas de keyword search).
     */
    private String extrairPrimeiraPalavra(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        String[] parts = t.split("\\s+");
        return parts.length > 0 ? parts[0] : null;
    }

    /**
     * Salva o livro/autor no banco e imprime dados no terminal.
     */
    private Optional<Livro> salvarLivro(GutenbergBook book) {
        if (book == null) return Optional.empty();

        System.out.println("\n=== DADOS DO LIVRO ENCONTRADO ===");
        System.out.println("Título: " + book.getTitle());
        if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {
            for (GutenbergAuthor a : book.getAuthors()) {
                System.out.println("Autor: " + a.getName()
                        + (a.getBirthYear() != null ? " (nasc. " + a.getBirthYear() + ")" : "")
                        + (a.getDeathYear() != null ? " - falec. " + a.getDeathYear() : ""));
            }
        }
        System.out.println("Idiomas: " + book.getLanguages());
        System.out.println("Número de downloads: " + book.getDownloadCount());
        if (book.getFormats() != null) {
            System.out.println("Formatos disponíveis:");
            book.getFormats().forEach((k, v) -> System.out.println(" - " + k + ": " + v));
        }
        System.out.println("=================================\n");

        // persistência básica: cria autor + livro
        GutenbergAuthor authorData = (book.getAuthors() != null && !book.getAuthors().isEmpty())
                ? book.getAuthors().get(0)
                : null;

        Autor autor = new Autor();
        // usar setName / setBirthYear / setDeathYear conforme entidade atual
        autor.setName(authorData != null ? authorData.getName() : "Desconhecido");
        autor.setBirthYear(authorData != null ? authorData.getBirthYear() : null);
        autor.setDeathYear(authorData != null ? authorData.getDeathYear() : null);
        autor = autorRepo.save(autor);

        Livro livro = new Livro();
        livro.setTitulo(book.getTitle());
        livro.setIdioma(book.getLanguages() != null && !book.getLanguages().isEmpty()
                ? book.getLanguages().get(0)
                : "pt");
        livro.setDownloads(book.getDownloadCount() != null ? book.getDownloadCount() : 0);
        livro.setAutor(autor);
        livro = livroRepo.save(livro);

        System.out.println("Livro persistido no banco: " + livro);
        return Optional.of(livro);
    }

    /**
     * Normaliza strings (remove acentos e coloca em minúsculas) para busca tolerante.
     */
    private static String normalize(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD);
        return n.replaceAll("\\p{M}", "").toLowerCase().trim();
    }
}