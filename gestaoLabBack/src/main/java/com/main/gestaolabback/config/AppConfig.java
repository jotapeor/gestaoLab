package com.main.gestaolabback.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    private String fusoHorario = "America/Cuiaba";
    private Reserva reserva = new Reserva();

    public String getFusoHorario() { return fusoHorario; }
    public void setFusoHorario(String fusoHorario) { this.fusoHorario = fusoHorario; }
    public Reserva getReserva() { return reserva; }
    public void setReserva(Reserva reserva) { this.reserva = reserva; }

    public static class Reserva {
        private int duracaoMinimaMinutos = 15;
        private int duracaoMaximaHoras = 12;
        private int antecedenciaMaximaDias = 60;

        public int getDuracaoMinimaMinutos() { return duracaoMinimaMinutos; }
        public void setDuracaoMinimaMinutos(int v) { this.duracaoMinimaMinutos = v; }
        public int getDuracaoMaximaHoras() { return duracaoMaximaHoras; }
        public void setDuracaoMaximaHoras(int v) { this.duracaoMaximaHoras = v; }
        public int getAntecedenciaMaximaDias() { return antecedenciaMaximaDias; }
        public void setAntecedenciaMaximaDias(int v) { this.antecedenciaMaximaDias = v; }
    }
}
