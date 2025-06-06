package com.example.Screenmatch.DTO;

import com.example.Screenmatch.model.Categoria;

public record SerieDTO (Long id,
    String titulo,
    Integer totalTemporadas,
    Double avaliacao,
    Categoria genero,
    String atores,
    String poster,
    String sinopse){
    
}
