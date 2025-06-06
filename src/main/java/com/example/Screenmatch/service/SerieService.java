package com.example.Screenmatch.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.example.Screenmatch.DTO.SerieDTO;
import com.example.Screenmatch.DTO.EpisodioDTO;
import com.example.Screenmatch.model.Categoria;
import com.example.Screenmatch.model.Serie;
import com.example.Screenmatch.repository.SerieRepository;

@Service
public class SerieService {
    @Autowired
    private SerieRepository repositorio;
    
    public List<SerieDTO> retornarSeries(){
        return converteSerie(repositorio.findAll());
    }

    public List<SerieDTO> obterTop5Series() {
        return converteSerie(repositorio.findTop5ByOrderByAvaliacaoDesc());
    }

    private List<SerieDTO> converteSerie(List<Serie> series){
        return series.stream()
                .map(s -> new SerieDTO
                (s.getId(), s.getTitulo(), s.getTotalTemporadas(), s.getAvaliacao(), s.getGenero(), s.getAtores(), s.getPoster(), s.getSinopse()))
                .collect(Collectors.toList());
    }

    public List<SerieDTO> obterLancamentos(){
        return converteSerie(repositorio.lancamentosMaisRecentes(PageRequest.of(0, 5)));
    }

    public SerieDTO obterPorId(Long id) {
        Optional<Serie> serie = repositorio.findById(id);
        if (serie.isPresent()) {
            Serie s = serie.get();
            return new SerieDTO(s.getId(), s.getTitulo(), s.getTotalTemporadas(), s.getAvaliacao(), s.getGenero(), s.getAtores(), s.getPoster(), s.getSinopse());
        }
        return null;
    }

    public List<EpisodioDTO> obterTodasTemporadas(Long id) {
        Optional<Serie> serie = repositorio.findById(id);
        if (serie.isPresent()) {
            Serie s = serie.get();
            return s.getEpisodios().stream()
                    .map(e -> new EpisodioDTO(e.getTemporada(), e.getNumeroEpisodio(), e.getTitulo()))
                    .collect(Collectors.toList());
        }
        return null;
    }

    public List<EpisodioDTO> obterTemporada(Long id, Long numero) {
        return repositorio.obterEpisodiosPorTemporada(id, numero).stream()
                    .map(e -> new EpisodioDTO(e.getTemporada(), e.getNumeroEpisodio(), e.getTitulo()))
                    .collect(Collectors.toList());
    }

    public List<SerieDTO> obterSeriesPorCategoria(String nomeGenero) {
        Categoria categoria = Categoria.fromPortugues(nomeGenero);
        return converteSerie((repositorio.findByGenero(categoria)));
    }
}
