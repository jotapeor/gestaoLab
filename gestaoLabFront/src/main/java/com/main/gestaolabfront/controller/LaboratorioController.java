package com.main.gestaolabfront.controller;

import tools.jackson.databind.ObjectMapper;
import com.main.gestaolabfront.service.LaboratorioApiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/laboratorios")
public class LaboratorioController {

    private final LaboratorioApiService laboratorioApiService;
    private final ObjectMapper objectMapper;

    public LaboratorioController(LaboratorioApiService laboratorioApiService, ObjectMapper objectMapper) {
        this.laboratorioApiService = laboratorioApiService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public String listar(HttpSession session, Model model,
                         @RequestParam(required = false) String nome,
                         @RequestParam(required = false) Boolean ativo) {
        if (!isCoordenador(session)) return "redirect:/acesso-negado";
        try {
            List<Map<String, Object>> lista = laboratorioApiService.listar(nome, ativo);
            model.addAttribute("laboratorios", lista != null ? lista : List.of());
        } catch (HttpClientErrorException.Forbidden e) {
            return "redirect:/acesso-negado";
        } catch (Exception e) {
            model.addAttribute("laboratorios", List.of());
        }
        model.addAttribute("menuAtivo", "laboratorios");
        model.addAttribute("filtroBusca", nome);
        model.addAttribute("filtroAtivo", ativo);
        model.addAttribute("urlVoltar", "/dashboard");
        return "laboratorios/lista";
    }

    @GetMapping("/novo")
    public String novoForm(HttpSession session, Model model) {
        if (!isCoordenador(session)) return "redirect:/acesso-negado";
        model.addAttribute("menuAtivo", "laboratorios");
        model.addAttribute("urlVoltar", "/laboratorios");
        return "laboratorios/form";
    }

    @PostMapping("/salvar")
    public String salvar(@RequestParam String nome,
                         @RequestParam(required = false) String localizacao,
                         @RequestParam(required = false) String capacidadeStr,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        if (!isCoordenador(session)) return "redirect:/acesso-negado";
        try {
            Integer capacidade = parsarCapacidade(capacidadeStr);
            laboratorioApiService.criar(nome, localizacao, capacidade);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Laboratório cadastrado com sucesso!");
            return "redirect:/laboratorios";
        } catch (HttpClientErrorException ex) {
            redirectAttributes.addFlashAttribute("formError",
                    extrairMensagem(ex.getResponseBodyAsString(), "Erro ao cadastrar."));
            redirectAttributes.addFlashAttribute("formNome", nome);
            redirectAttributes.addFlashAttribute("formLocalizacao", localizacao);
            redirectAttributes.addFlashAttribute("formCapacidade", capacidadeStr);
            return "redirect:/laboratorios/novo";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("formError", "Erro inesperado ao cadastrar.");
            redirectAttributes.addFlashAttribute("formNome", nome);
            redirectAttributes.addFlashAttribute("formLocalizacao", localizacao);
            redirectAttributes.addFlashAttribute("formCapacidade", capacidadeStr);
            return "redirect:/laboratorios/novo";
        }
    }

    @GetMapping("/editar/{id}")
    public String editarForm(@PathVariable Long id, HttpSession session, Model model,
                             RedirectAttributes redirectAttributes) {
        if (!isCoordenador(session)) return "redirect:/acesso-negado";
        try {
            Map<String, Object> lab = laboratorioApiService.buscarPorId(id);
            model.addAttribute("laboratorio", lab);
            model.addAttribute("edicao", true);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Laboratório não encontrado.");
            return "redirect:/laboratorios";
        }
        model.addAttribute("menuAtivo", "laboratorios");
        model.addAttribute("urlVoltar", "/laboratorios");
        return "laboratorios/form";
    }

    @PostMapping("/editar/{id}")
    public String salvarEdicao(@PathVariable Long id,
                               @RequestParam String nome,
                               @RequestParam(required = false) String localizacao,
                               @RequestParam(required = false) String capacidadeStr,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (!isCoordenador(session)) return "redirect:/acesso-negado";
        try {
            Integer capacidade = parsarCapacidade(capacidadeStr);
            laboratorioApiService.atualizar(id, nome, localizacao, capacidade);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Laboratório atualizado com sucesso!");
            return "redirect:/laboratorios";
        } catch (HttpClientErrorException ex) {
            redirectAttributes.addFlashAttribute("formError",
                    extrairMensagem(ex.getResponseBodyAsString(), "Erro ao atualizar."));
            redirectAttributes.addFlashAttribute("formNome", nome);
            redirectAttributes.addFlashAttribute("formLocalizacao", localizacao);
            redirectAttributes.addFlashAttribute("formCapacidade", capacidadeStr);
            return "redirect:/laboratorios/editar/" + id;
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("formError", "Erro inesperado ao atualizar.");
            redirectAttributes.addFlashAttribute("formNome", nome);
            redirectAttributes.addFlashAttribute("formLocalizacao", localizacao);
            redirectAttributes.addFlashAttribute("formCapacidade", capacidadeStr);
            return "redirect:/laboratorios/editar/" + id;
        }
    }

    @PostMapping("/{id}/inativar")
    public String inativar(@PathVariable Long id, HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (!isCoordenador(session)) return "redirect:/acesso-negado";
        try {
            laboratorioApiService.inativar(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Laboratório inativado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao inativar.");
        }
        return "redirect:/laboratorios";
    }

    @PostMapping("/{id}/reativar")
    public String reativar(@PathVariable Long id, HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (!isCoordenador(session)) return "redirect:/acesso-negado";
        try {
            laboratorioApiService.reativar(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Laboratório reativado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao reativar.");
        }
        return "redirect:/laboratorios";
    }

    private boolean isCoordenador(HttpSession session) {
        return "COORDENADOR".equals(session.getAttribute("perfil"));
    }

    private Integer parsarCapacidade(String capacidadeStr) {
        if (capacidadeStr == null || capacidadeStr.isBlank()) return null;
        try { return Integer.parseInt(capacidadeStr.trim()); } catch (NumberFormatException e) { return null; }
    }

    @SuppressWarnings("unchecked")
    private String extrairMensagem(String responseBody, String fallback) {
        if (responseBody == null || responseBody.isBlank()) return fallback;
        try {
            Map<String, Object> map = objectMapper.readValue(responseBody, Map.class);
            Object msg = map.get("message");
            return msg != null ? msg.toString() : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }
}
