package com.main.gestaolabfront.dto;

public interface ComTipoLabel {
    String tipo();

    default String tipoLabel() {
        if (tipo() == null) return "Outro";
        return switch (tipo()) {
            case "TCC_I"     -> "TCC I";
            case "TCC_II"    -> "TCC II";
            case "EXTENSAO"  -> "Extensão";
            case "MONITORIA" -> "Monitoria";
            case "BOLSISTA"  -> "Bolsista";
            case "PESQUISA"  -> "Pesquisa";
            case "AULA"      -> "Aula";
            default          -> "Outro";
        };
    }
}
