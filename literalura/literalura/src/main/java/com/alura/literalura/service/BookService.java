package com.alura.literalura.service;

import com.alura.literalura.model.Autor;
import com.alura.literalura.model.Livro;
import com.alura.literalura.repository.AutorRepository;
import com.alura.literalura.repository.LivroRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final LivroRepository livroRepository;
    private final AutorRepository autorRepository;

    public BookService(LivroRepository livroRepository, AutorRepository autorRepository) {
        this.livroRepository = livroRepository;
        this.autorRepository = autorRepository;
    }

    public List<Livro> listarTodosLivros() {
        return livroRepository.findAll();
    }

    public List<Autor> listarTodosAutores() {
        return autorRepository.findAll();
    }

    public List<Autor> autoresVivosEm(int ano) {
        return autorRepository.findByBirthYearLessThanEqualAndDeathYearGreaterThanEqual(ano, ano);
    }

    public List<Livro> livrosPorIdioma(String idioma) {
        return livroRepository.findByIdioma(idioma.toLowerCase());
    }
}