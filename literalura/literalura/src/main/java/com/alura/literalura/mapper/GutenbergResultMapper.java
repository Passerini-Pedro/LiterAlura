package com.alura.literalura.mapper;

import com.alura.literalura.dto.GutenbergAuthor;
import com.alura.literalura.dto.GutenbergBook;
import com.alura.literalura.model.Autor;
import com.alura.literalura.model.Livro;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GutenbergResultMapper {

    /**
     * Mapeia um DTO GutenbergBook para entidade Livro.
     * Não persiste nada no banco — apenas monta as entidades.
     */
    public static Livro toLivro(GutenbergBook book) {
        if (book == null) return null;

        Livro livro = new Livro();
        livro.setTitulo(Optional.ofNullable(book.getTitle()).orElse(""));

        String idioma = "pt";
        if (book.getLanguages() != null && !book.getLanguages().isEmpty()) {
            idioma = Optional.ofNullable(book.getLanguages().get(0)).orElse(idioma);
        }
        livro.setIdioma(idioma);

        livro.setDownloads(Optional.ofNullable(book.getDownloadCount()).orElse(0));

        // mapeia o primeiro autor (se existir) para Autor embutido na entidade Livro
        if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {
            GutenbergAuthor gAuthor = book.getAuthors().get(0);
            Autor autor = new Autor();
            autor.setName(Optional.ofNullable(gAuthor.getName()).orElse("Desconhecido"));
            autor.setBirthYear(gAuthor.getBirthYear());
            autor.setDeathYear(gAuthor.getDeathYear());
            livro.setAutor(autor);
        } else {
            Autor autor = new Autor();
            autor.setName("Desconhecido");
            livro.setAutor(autor);
        }

        return livro;
    }

    /**
     * Mapeia um JsonNode (um item dentro de "results") para Livro.
     * Útil caso você esteja trabalhando direto com JsonNode ao invés de DTOs.
     */
    public static Livro toLivro(JsonNode itemNode) {
        if (itemNode == null || itemNode.isMissingNode()) return null;

        Livro livro = new Livro();
        livro.setTitulo(itemNode.path("title").asText(""));

        // idioma (pega primeiro elemento do array "languages" se existir)
        String idioma = "pt";
        JsonNode langs = itemNode.path("languages");
        if (langs.isArray() && langs.size() > 0) {
            idioma = langs.get(0).asText(idioma);
        }
        livro.setIdioma(idioma);

        livro.setDownloads(itemNode.path("download_count").isInt()
                ? itemNode.path("download_count").asInt() : 0);

        // autor (pega o primeiro elemento do array authors)
        JsonNode authors = itemNode.path("authors");
        if (authors.isArray() && authors.size() > 0) {
            JsonNode a = authors.get(0);
            Autor autor = new Autor();
            autor.setName(a.path("name").asText("Desconhecido"));
            // tenta parsear ano de nascimento/falecimento (se existir)
            if (a.has("birth_year") && !a.get("birth_year").isNull()) {
                try {
                    autor.setBirthYear(a.get("birth_year").asInt());
                } catch (Exception ignored) {}
            }
            if (a.has("death_year") && !a.get("death_year").isNull()) {
                try {
                    autor.setDeathYear(a.get("death_year").asInt());
                } catch (Exception ignored) {}
            }
            livro.setAutor(autor);
        } else {
            Autor autor = new Autor();
            autor.setName("Desconhecido");
            livro.setAutor(autor);
        }

        return livro;
    }

    /**
     * Mapeia uma lista de GutenbergBook para lista de Livro (não persiste).
     */
    public static List<Livro> toLivroList(List<GutenbergBook> books) {
        List<Livro> lista = new ArrayList<>();
        if (books == null) return lista;
        for (GutenbergBook b : books) {
            Livro l = toLivro(b);
            if (l != null) lista.add(l);
        }
        return lista;
    }
}