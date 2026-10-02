package com.main.gestaolabfront.controller;

import com.main.gestaolabfront.service.CursoSetorApiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cursos-setores")
public class CursoSetorController {

    private final CursoSetorApiService cursoSetorApiService;

    public CursoSetorController(CursoSetorApiService cursoSetorApiService) {
        this.cursoSetorApiService = cursoSetorApiService;
    }

    @GetMapping
    public String listar(HttpSession session, Model model,
                         @RequestParam(required = false) String nome,
                         @RequestParam(required = false) Boolean ativo,
                         @RequestParam(required = false) String tipo) {
        if (!isCoordenador(session)) return "redirect:/acesso-negado";
        try {
            List<Map<String, Object>> lista = cursoSetorApiService.listar(nome, ativo, tipo);
            model.addAttribute("cursos", lista != null ? lista : List.of());
        } catch (HttpClientErrorException.Forbidden e) {
            return "redirect:/acesso-negado";
        } catch (Exception e) {
            model.addAttribute("cursos", List.of());
        }
        model.addAttribute("menuAtivo", "cursos-setores");
        model.addAttribute("filtroBusca", nome);
        model.addAttribute("filtroAtivo", ativo);
        model.addAttribute("filtroTipo", tipo);
        return "cursos-setores/lista";
    }

    @GetMapping("/novo")
    public String novoForm(HttpSession session, Model model) {
        if (!isCoordenador(session)) return "redirect:/acesso-negado";
        model.addAttribute("menuAtivo", "cursos-setores");
        return "cursos-setores/form";
    }

    @PostMapping("/salvar")
    public String salvar(@RequestParam String nome,
                         @RequestParam(defaultValue = "CURSO") String tipo,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        if (!isCoordenador(session)) return "redirect:/acesso-negado";
        try {
            cursoSetorApiService.criar(nome, tipo);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Curso/Setor cadastrado com sucesso!");
            return "redirect:/cursos-setores";
        } catch (HttpClientErrorException ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    extrairMensagem(ex.getResponseBodyAsString(), "Erro ao cadastrar."));
            return "redirect:/cursos-setores/novo";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro inesperado ao cadastrar.");
            return "redirect:/cursos-setores/novo";
        }
    }

    @GetMapping("/editar/{id}")
    public String editarForm(@PathVariable Long id, HttpSession session, Model model,
                             RedirectAttributes redirectAttributes) {
        if (!isCoordenador(session)) return "redirect:/acesso-negado";
        try {
            Map<String, Object> cs = cursoSetorApiService.buscarPorId(id);
            model.addAttribute("cursoSetor", cs);
            model.addAttribute("edicao", true);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Curso/Setor não encontrado.");
            return "redirect:/cursos-setores";
        }
        model.addAttribute("menuAtivo", "cursos-setores");
        return "cursos-setores/form";
    }

    @PostMapping("/editar/{id}")
    public String salvarEdicao(@PathVariable Long id,
                               @RequestParam String nome,
                               @RequestParam(defaultValue = "CURSO") String tipo,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (!isCoordenador(session)) return "redirect:/acesso-negado";
        try {
            cursoSetorApiService.atualizar(id, nome, tipo);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Curso/Setor atualizado com sucesso!");
            return "redirect:/cursos-setores";
        } catch (HttpClientErrorException ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    extrairMensagem(ex.getResponseBodyAsString(), "Erro ao atualizar."));
            return "redirect:/cursos-setores/editar/" + id;
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro inesperado ao atualizar.");
            return "redirect:/cursos-setores/editar/" + id;
        }
    }

    @PostMapping("/{id}/inativar")
    public String inativar(@PathVariable Long id, HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (!isCoordenador(session)) return "redirect:/acesso-negado";
        try {
            cursoSetorApiService.inativar(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Curso/Setor inativado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao inativar.");
        }
        return "redirect:/cursos-setores";
    }

    @PostMapping("/{id}/reativar")
    public String reativar(@PathVariable Long id, HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (!isCoordenador(session)) return "redirect:/acesso-negado";
        try {
            cursoSetorApiService.reativar(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Curso/Setor reativado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao reativar.");
        }
        return "redirect:/cursos-setores";
    }

    private boolean isCoordenador(HttpSession session) {
        return "COORDENADOR".equals(session.getAttribute("perfil"));
    }

    private String extrairMensagem(String responseBody, String fallback) {
        if (responseBody == null || responseBody.isBlank()) return fallback;
        int idx = responseBody.indexOf("\"message\":\"");
        if (idx == -1) return fallback;
        int start = idx + "\"message\":\"".length();
        int end = responseBody.indexOf("\"", start);
        return end == -1 ? fallback : responseBody.substring(start, end);
    }
}
