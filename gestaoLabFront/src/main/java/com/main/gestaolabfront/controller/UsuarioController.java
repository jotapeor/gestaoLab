package com.main.gestaolabfront.controller;

import tools.jackson.databind.ObjectMapper;
import com.main.gestaolabfront.dto.UsuarioDto;
import com.main.gestaolabfront.service.ProjetoApiService;
import com.main.gestaolabfront.service.UsuarioApiService;
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
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioApiService usuarioApiService;
    private final CursoSetorApiService cursoSetorApiService;
    private final ProjetoApiService projetoApiService;
    private final ObjectMapper objectMapper;

    public UsuarioController(UsuarioApiService usuarioApiService,
                              CursoSetorApiService cursoSetorApiService,
                              ProjetoApiService projetoApiService,
                              ObjectMapper objectMapper) {
        this.usuarioApiService = usuarioApiService;
        this.cursoSetorApiService = cursoSetorApiService;
        this.projetoApiService = projetoApiService;
        this.objectMapper = objectMapper;
    }

    // ---- Listagem ----

    @GetMapping
    public String listar(HttpSession session, Model model,
                         @RequestParam(required = false) String busca,
                         @RequestParam(required = false) String perfil,
                         @RequestParam(required = false) Long cursoSetorId,
                         @RequestParam(required = false) Boolean ativo,
                         @RequestParam(defaultValue = "0") int page) {
        String perfilSessao = (String) session.getAttribute("perfil");
        if (!isCoordenadorOuProfessor(session)) return "redirect:/acesso-negado";
        try {
            Map<String, Object> resultado = usuarioApiService.listar(busca, perfil, cursoSetorId, ativo, page);
            model.addAttribute("pagina", resultado);
            model.addAttribute("isCoordenador", "COORDENADOR".equals(perfilSessao));
        } catch (HttpClientErrorException.Forbidden e) {
            return "redirect:/acesso-negado";
        } catch (Exception e) {
            model.addAttribute("pagina", Map.of("content", List.of(), "totalPages", 0, "number", 0));
            model.addAttribute("isCoordenador", "COORDENADOR".equals(perfilSessao));
        }
        carregarCursosAtivosNoModel(model);
        model.addAttribute("menuAtivo", "usuarios");
        model.addAttribute("filtroBusca", busca);
        model.addAttribute("filtroPerfil", perfil);
        model.addAttribute("filtroCursoSetorId", cursoSetorId);
        model.addAttribute("filtroAtivo", ativo);
        model.addAttribute("pageAtual", page);
        model.addAttribute("urlVoltar", "/dashboard");
        return "usuarios/lista";
    }

    // ---- Cadastro ----

    @GetMapping("/novo")
    public String novoForm(HttpSession session, Model model) {
        if (!isCoordenador(session)) return "redirect:/acesso-negado";
        carregarSelectsNoModel(model);
        model.addAttribute("menuAtivo", "usuarios");
        model.addAttribute("urlVoltar", "/usuarios");
        return "usuarios/form";
    }

    @PostMapping("/salvar")
    public String salvar(@RequestParam String nome,
                         @RequestParam(required = false) String matricula,
                         @RequestParam String email,
                         @RequestParam(defaultValue = "USUARIO") String perfil,
                         @RequestParam(required = false) Long cursoSetorId,
                         @RequestParam(required = false) Long responsavelId,
                         @RequestParam String senhaProvisoria,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        if (!isCoordenador(session)) return "redirect:/acesso-negado";
        try {
            Map<String, Object> body = buildBody(nome, matricula, email, perfil, cursoSetorId, responsavelId);
            body.put("senhaProvisoria", senhaProvisoria);
            usuarioApiService.criar(body);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuário cadastrado com sucesso!");
            return "redirect:/usuarios";
        } catch (HttpClientErrorException ex) {
            redirectAttributes.addFlashAttribute("formError",
                    extrairMensagem(ex.getResponseBodyAsString(), "Erro ao cadastrar."));
            preservarFormulario(redirectAttributes, nome, matricula, email, perfil, cursoSetorId, responsavelId);
            return "redirect:/usuarios/novo";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("formError", "Erro inesperado ao cadastrar.");
            preservarFormulario(redirectAttributes, nome, matricula, email, perfil, cursoSetorId, responsavelId);
            return "redirect:/usuarios/novo";
        }
    }

    // ---- Edição ----

    @GetMapping("/editar/{id}")
    public String editarForm(@PathVariable Long id, HttpSession session, Model model,
                             RedirectAttributes redirectAttributes) {
        if (!isCoordenador(session)) return "redirect:/acesso-negado";
        try {
            UsuarioDto usuario = usuarioApiService.buscarPorId(id);
            model.addAttribute("usuario", usuario);
            model.addAttribute("edicao", true);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Usuário não encontrado.");
            return "redirect:/usuarios";
        }
        carregarSelectsNoModel(model);
        model.addAttribute("menuAtivo", "usuarios");
        model.addAttribute("urlVoltar", "/usuarios/" + id);
        return "usuarios/form";
    }

    @PostMapping("/editar/{id}")
    public String salvarEdicao(@PathVariable Long id,
                               @RequestParam String nome,
                               @RequestParam(required = false) String matricula,
                               @RequestParam String email,
                               @RequestParam(defaultValue = "USUARIO") String perfil,
                               @RequestParam(required = false) Long cursoSetorId,
                               @RequestParam(required = false) Long responsavelId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (!isCoordenador(session)) return "redirect:/acesso-negado";
        try {
            Map<String, Object> body = buildBody(nome, matricula, email, perfil, cursoSetorId, responsavelId);
            usuarioApiService.atualizar(id, body);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuário atualizado com sucesso!");
            return "redirect:/usuarios";
        } catch (HttpClientErrorException ex) {
            redirectAttributes.addFlashAttribute("formError",
                    extrairMensagem(ex.getResponseBodyAsString(), "Erro ao atualizar."));
            preservarFormulario(redirectAttributes, nome, matricula, email, perfil, cursoSetorId, responsavelId);
            return "redirect:/usuarios/editar/" + id;
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("formError", "Erro inesperado ao atualizar.");
            preservarFormulario(redirectAttributes, nome, matricula, email, perfil, cursoSetorId, responsavelId);
            return "redirect:/usuarios/editar/" + id;
        }
    }

    // ---- Inativar / Reativar ----

    @PostMapping("/{id}/inativar")
    public String inativar(@PathVariable Long id, HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (!isCoordenador(session)) return "redirect:/acesso-negado";
        try {
            usuarioApiService.inativar(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuário inativado com sucesso!");
        } catch (HttpClientErrorException ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    extrairMensagem(ex.getResponseBodyAsString(), "Erro ao inativar."));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao inativar.");
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/reativar")
    public String reativar(@PathVariable Long id, HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (!isCoordenador(session)) return "redirect:/acesso-negado";
        try {
            usuarioApiService.reativar(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuário reativado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao reativar.");
        }
        return "redirect:/usuarios";
    }

    // ---- Redefinir senha ----

    @PostMapping("/{id}/redefinir-senha")
    public String redefinirSenha(@PathVariable Long id,
                                 @RequestParam String novaSenhaProvisoria,
                                 @RequestParam String confirmacaoSenha,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (!isCoordenador(session)) return "redirect:/acesso-negado";
        if (!novaSenhaProvisoria.equals(confirmacaoSenha)) {
            redirectAttributes.addFlashAttribute("errorMessage", "As senhas não conferem.");
            return "redirect:/usuarios";
        }
        try {
            usuarioApiService.redefinirSenha(id, novaSenhaProvisoria);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Senha redefinida com sucesso!");
        } catch (HttpClientErrorException ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    extrairMensagem(ex.getResponseBodyAsString(), "Erro ao redefinir senha."));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao redefinir senha.");
        }
        return "redirect:/usuarios";
    }

    // ---- Detalhe ----

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, HttpSession session, Model model,
                          RedirectAttributes redirectAttributes) {
        if (!isCoordenadorOuProfessor(session)) return "redirect:/acesso-negado";
        UsuarioDto usuario;
        try {
            usuario = usuarioApiService.buscarPorId(id);
            model.addAttribute("usuario", usuario);
        } catch (HttpClientErrorException.Forbidden e) {
            return "redirect:/acesso-negado";
        } catch (HttpClientErrorException.NotFound e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Usuário não encontrado.");
            return "redirect:/usuarios";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao carregar usuário.");
            return "redirect:/usuarios";
        }
        try {
            String perfilUsuario = usuario.perfil();
            java.util.List<java.util.Map<String, Object>> projetos;
            if ("USUARIO".equals(perfilUsuario)) {
                projetos = projetoApiService.listarPorParticipante(id);
            } else {
                projetos = projetoApiService.listarPorOrientador(id);
            }
            model.addAttribute("projetosDoUsuario", projetos);
        } catch (Exception e) {
            model.addAttribute("projetosDoUsuario", java.util.List.of());
        }
        model.addAttribute("menuAtivo", "usuarios");
        model.addAttribute("isCoordenador", isCoordenador(session));
        model.addAttribute("urlVoltar", "/usuarios");
        return "usuarios/detalhe";
    }

    // ---- Helpers ----

    private boolean isCoordenador(HttpSession session) {
        return "COORDENADOR".equals(session.getAttribute("perfil"));
    }

    private boolean isCoordenadorOuProfessor(HttpSession session) {
        String p = (String) session.getAttribute("perfil");
        return "COORDENADOR".equals(p) || "PROFESSOR".equals(p);
    }

    private void carregarCursosAtivosNoModel(Model model) {
        try {
            model.addAttribute("cursosAtivos", cursoSetorApiService.listar(null, true, null));
        } catch (Exception e) {
            model.addAttribute("cursosAtivos", List.of());
        }
    }

    private void carregarSelectsNoModel(Model model) {
        carregarCursosAtivosNoModel(model);
        try {
            model.addAttribute("responsaveis", usuarioApiService.listarResponsaveis());
        } catch (Exception e) {
            model.addAttribute("responsaveis", List.of());
        }
    }

    private Map<String, Object> buildBody(String nome, String matricula, String email,
                                           String perfil, Long cursoSetorId, Long responsavelId) {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("nome", nome);
        body.put("email", email);
        body.put("perfil", perfil);
        if (matricula != null && !matricula.isBlank()) body.put("matricula", matricula);
        if (cursoSetorId != null) body.put("cursoSetorId", cursoSetorId);
        if (responsavelId != null) body.put("responsavelId", responsavelId);
        return body;
    }

    private void preservarFormulario(RedirectAttributes redirectAttributes,
                                      String nome, String matricula, String email,
                                      String perfil, Long cursoSetorId, Long responsavelId) {
        redirectAttributes.addFlashAttribute("formNome", nome);
        redirectAttributes.addFlashAttribute("formMatricula", matricula);
        redirectAttributes.addFlashAttribute("formEmail", email);
        redirectAttributes.addFlashAttribute("formPerfil", perfil);
        redirectAttributes.addFlashAttribute("formCursoSetorId", cursoSetorId);
        redirectAttributes.addFlashAttribute("formResponsavelId", responsavelId);
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
