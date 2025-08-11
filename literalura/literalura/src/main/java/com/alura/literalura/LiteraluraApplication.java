package com.alura.literalura;

import com.alura.literalura.model.Autor;
import com.alura.literalura.model.Livro;
import com.alura.literalura.service.BookService;
import com.alura.literalura.service.GutenbergService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@SpringBootApplication
public class LiteraluraApplication {

	public static void main(String[] args) {
		SpringApplication.run(LiteraluraApplication.class, args);
	}

	@Bean
	public CommandLineRunner run(GutenbergService gutenbergService, BookService bookService) {
		return args -> {
			Scanner scanner = new Scanner(System.in);
			int opcao;

			do {
				System.out.println("\n=== MENU ===");
				System.out.println("1. Buscar livro pelo título");
				System.out.println("2. Listar livros registrados");
				System.out.println("3. Listar nossos autores");
				System.out.println("4. Listar autores em determinado ano");
				System.out.println("5. Listar livros em determinado idioma");
				System.out.println("0. Sair");
				System.out.print("Escolha uma opção: ");

				opcao = scanner.nextInt();
				scanner.nextLine();

				switch (opcao) {
					case 1 -> {
						System.out.print("Informe o título: ");
						String titulo = scanner.nextLine();
						Optional<Livro> resultado = gutenbergService.buscarELancar(titulo);
						if (resultado.isPresent()) {
							System.out.println("Livro registrado: " + resultado.get());
						} else {
							System.out.println("Nenhum livro encontrado na API.");
						}
					}
					case 2 -> {
						List<Livro> livros = bookService.listarTodosLivros();
						System.out.println("\n-- Livros Registrados --");
						livros.forEach(System.out::println);
					}
					case 3 -> {
						List<Autor> autores = bookService.listarTodosAutores();
						System.out.println("\n-- Nossos Autores --");
						autores.forEach(System.out::println);
					}
					case 4 -> {
						System.out.print("Informe o ano: ");
						int ano = scanner.nextInt();
						List<Autor> vivos = bookService.autoresVivosEm(ano);
						System.out.println("\n-- Autores vivos em " + ano + " --");
						vivos.forEach(System.out::println);
					}
					case 5 -> {
						System.out.print("Informe o idioma (pt, en, es, fr): ");
						String idioma = scanner.nextLine();
						List<Livro> porIdioma = bookService.livrosPorIdioma(idioma);
						System.out.println("\n-- Livros em " + idioma + " --");
						if (porIdioma.isEmpty()) {
							System.out.println("Nenhum livro encontrado nesse idioma.");
						} else {
							porIdioma.forEach(System.out::println);
						}
					}
					case 0 -> System.out.println("Encerrando aplicação.");
					default -> System.out.println("Opção inválida. Tente novamente.");
				}
			} while (opcao != 0);

			scanner.close();
		};
	}
}