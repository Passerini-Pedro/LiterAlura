package com.alura.literalura.repository;

import com.alura.literalura.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LivroRepository extends JpaRepository<Livro, Long> {
    List<Livro> findByIdioma(String idioma);
    long countByIdioma(String idioma);

    // Método para apagar todos os livros (opcional, pois deleteAll() já existe no JpaRepository)
    default void apagarTodosLivros() {
        deleteAll();
    }
}