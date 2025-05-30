package com.example.Screenmatch.principal;

import com.example.Screenmatch.model.DadosSerie;
import com.example.Screenmatch.model.DadosTemporada;
import com.example.Screenmatch.model.Episodio;
import com.example.Screenmatch.model.Serie;
import com.example.Screenmatch.service.ConsomeApi;
import com.example.Screenmatch.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsomeApi consumo = new ConsomeApi();
    private ConverteDados conversor = new ConverteDados();

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";

    private List<DadosSerie> dadosSeries = new ArrayList<>();

    public void exibeMenu(){
        var opcao = -1;
        while (opcao != 0){
            var menu = 
            """
                1 - Buscar todos os episódios de uma série
                2 - Buscar um episódio específico
                3 - Buscar uma série
                4 - Listar séries buscadas

                0 - Sair                  
            """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1 -> printaEpisodios(retornaEpisodios(getDados()));
                case 2 -> procuraEpisodio(retornaEpisodios(getDados()));
                case 3 -> printaSerie(getDados());
                case 4 -> listarBuscadas();
                case 0 -> System.out.println("Saindo...");
                default -> System.out.println("Opção inválida");
            }
        }
    }

    public DadosSerie getDados(){
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine().replace(" ", "+");

        var json = consumo.obterDados(ENDERECO + nomeSerie + API_KEY);
        return conversor.obterDados(json, DadosSerie.class);
    }

    public void listarBuscadas(){
        List<Serie> series = new ArrayList<>();

        series = dadosSeries.stream()
                .map(d -> new Serie(d))
                .collect(Collectors.toList());
        series.stream() 
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    public List<Episodio> retornaEpisodios(DadosSerie dados){
        List<DadosTemporada> temporadas = new ArrayList<>();

        for (int i = 1; i<=dados.totalTemporadas(); i++){
            var json = consumo.obterDados(ENDERECO + dados.titulo().replace(" ", "+") +"&season=" + i + API_KEY);
            DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }

        return temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                .map(d -> new Episodio(t.numero(), d)))
                .collect(Collectors.toList());
    }

    public void printaSerie(DadosSerie dados){
        dadosSeries.add(dados);
        

        System.out.println(dados);
    }

    public void printaEpisodios(List<Episodio> episodios){
        episodios.forEach(System.out::print);
    }

    public void procuraEpisodio(List<Episodio> episodios){
        System.out.println("Digite um trecho do título do episódio:");
        var trechoTitulo = leitura.nextLine();
        Optional<Episodio> episodioBuscado = episodios.stream()
            .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
            .findFirst();
        if(episodioBuscado.isPresent()){
            System.out.println("Episódio encontrado!");
            System.out.println(episodioBuscado);
        } else {
            System.out.println("Episódio não encontrado!");
        }
    }

    public void statsTemporada(List<Episodio> episodios){
        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada, Collectors.averagingDouble(Episodio::getAvaliacao)));

        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
        
        System.out.printf("\nNota média da série: %0.2f\n", est.getAverage());
        System.out.println("Nota média por temporada: " + avaliacoesPorTemporada);
        System.out.printf("Melhor episódio: %.2f (%s)\n", est.getMax(), episodios.stream().filter(e -> e.getAvaliacao() == est.getMax()).findFirst().get().getTitulo());
        System.out.printf("Pior episódio: %.2f (%s)\n", est.getMin(), episodios.stream().filter(e -> e.getAvaliacao() == est.getMin()).findFirst().get().getTitulo());
        System.out.println("Quantidade de episódios: " + est.getCount());
    }

    public void buscarPorAno(List<Episodio> episodios){
        System.out.println("A partir de que ano você deseja ver os episódios? ");
        var ano = leitura.nextInt();
        leitura.nextLine();

        LocalDate dataBusca = LocalDate.of(ano, 1, 1);
        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        episodios.stream()
            .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
            .forEach(e -> 
            System.out.println(
                    "Temporada:  " + e.getTemporada() +
                    " Episódio: " + e.getTitulo() +
                    " Data lançamento: " + e.getDataLancamento().format(formatador)
            ));
    }
}