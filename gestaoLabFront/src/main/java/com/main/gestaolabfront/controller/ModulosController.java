package com.main.gestaolabfront.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ModulosController {

    @GetMapping("/usuarios")
    public String usuarios(Model model) {
        model.addAttribute("menuAtivo", "usuarios");
        model.addAttribute("tituloModulo", "Usuários");
        return "comum/em-construcao";
    }

    @GetMapping("/uso-laboratorio")
    public String usoLaboratorio(Model model) {
        model.addAttribute("menuAtivo", "uso-laboratorio");
        model.addAttribute("tituloModulo", "Uso do Laboratório");
        return "comum/em-construcao";
    }

    @GetMapping("/materiais")
    public String materiais(Model model) {
        model.addAttribute("menuAtivo", "materiais");
        model.addAttribute("tituloModulo", "Materiais");
        return "comum/em-construcao";
    }

    @GetMapping("/equipamentos")
    public String equipamentos(Model model) {
        model.addAttribute("menuAtivo", "equipamentos");
        model.addAttribute("tituloModulo", "Equipamentos");
        return "comum/em-construcao";
    }

    @GetMapping("/relatorios")
    public String relatorios(Model model) {
        model.addAttribute("menuAtivo", "relatorios");
        model.addAttribute("tituloModulo", "Relatórios");
        return "comum/em-construcao";
    }
}
