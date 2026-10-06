package com.main.gestaolabfront.controller;

import tools.jackson.databind.ObjectMapper;
import com.main.gestaolabfront.dto.ProjetoDetalheDto;
import com.main.gestaolabfront.service.ProjetoApiService;
import com.main.gestaolabfront.service.UsuarioApiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/projetos")
public class ProjetoController {

    private final ProjetoApiService projetoApiService;
    private final UsuarioApiService usuarioApiService;
    private final ObjectMapper objectMapper;

    public ProjetoController(ProjetoApiService projetoApiService,
                              UsuarioApiService usuarioApiService,
                              ObjectMapper objectMapper) {
        this.projetoApiService = projetoApiService;
        this.usuarioApiService = usuarioApiService;
        this.objectMapper = objectMapper;
    }

    // ---- Listagem (COORDENADOR / PROFESSOR) ----

    @GetMapping
    public String listar(HttpSession session, Model model,
                         @RequestParam(required = false) String busca,
                         @RequestParam(required = false) String tipo,
                         @RequestParam(required = false) Long orientadorId,
                         @RequestParam(required = false) Boolean ativo,
                         @RequestParam(defaultValue = "0") int page) {
        String perfil = (String) session.getAttribute("perfil");
        if ("USUARIO".equals(perfil)) {
            return "redirect:/projetos/meus";
        }
        if (!isCoordenadorOuProfessor(session)) return "redirect:/acesso-negado";

        try {
            Map<String, Object> resultado = projetoApiService.listar(busca, tipo, orientadorId, null, ativo, page);
            model.addAttribute("pagina", resultado);
        } catch (Exception e) {
            model.addAttribute("pagina", Map.of("content", List.of(), "totalPages", 0, "number", 0));
        }

        if (isCoordenador(session)) {
            try {
                model.addAttribute("responsaveis", usuarioApiService.listarResponsaveis());
            } catch (Exception e) {
                model.addAttribute("responsaveis", List.of());
            }
        }

        model.addAttribute("isCoordenador", isCoordenador(session));
        model.addAttribute("menuAtivo", "projetos");
        model.addAttribute("filtroBusca", busca);
        model.addAttribute("filtroTipo", tipo);
        model.addAttribute("filtroOrientadorId", orientadorId);
        model.addAttribute("filtroAtivo", ativo);
        model.addAttribute("pageAtual", page);
        return "projetos/lista";
    }

    // ---- Meus projetos (todos os perfis) ----

    @GetMapping("/meus")
    public String meusProjetos(HttpSession session, Model model) {
        try {
            List<Map<String, Object>> projetos = projetoApiService.meusProjetos();
            model.addAttribute("projetos", projetos);
        } catch (Exception e) {
            model.addAttribute("projetos", List.of());
        }
        model.addAttribute("menuAtivo", "projetos");
        model.addAttribute("isProfessorOuCoordenador", isCoordenadorOuProfessor(session));
        return "projetos/meus-projetos";
    }

    // ---- Cadastro ----

    @GetMapping("/novo")
    public String novoForm(HttpSession session, Model model) {
        if (!isCoordenadorOuProfessor(session)) return "redirect:/acesso-negado";
        carregarSelectsNoModel(model, session);
        model.addAttribute("menuAtivo", "projetos");
        return "projetos/form";
    }

    @PostMapping("/salvar")
    public String salvar(@RequestParam String titulo,
                         @RequestParam(required = false) String tipo,
                         @RequestParam(required = false) Long orientadorId,
                         @RequestParam(required = false) String descricao,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        if (!isCoordenadorOuProfessor(session)) return "redirect:/acesso-negado";
        try {
            Map<String, Object> body = buildBody(titulo, tipo, orientadorId, descricao, session);
            projetoApiService.criar(body);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Projeto cadastrado com sucesso!");
            return "redirect:/projetos";
        } catch (HttpClientErrorException ex) {
            redirectAttributes.addFlashAttribute("formError",
                    extrairMensagem(ex.getResponseBodyAsString(), "Erro ao cadastrar."));
            preservarFormulario(redirectAttributes, titulo, tipo, orientadorId, descricao);
            return "redirect:/projetos/novo";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("formError", "Erro inesperado ao cadastrar.");
            preservarFormulario(redirectAttributes, titulo, tipo, orientadorId, descricao);
            return "redirect:/projetos/novo";
        }
    }

    // ---- Edição ----

    @GetMapping("/editar/{id}")
    public String editarForm(@PathVariable Long id, HttpSession session, Model model,
                             RedirectAttributes redirectAttributes) {
        if (!isCoordenadorOuProfessor(session)) return "redirect:/acesso-negado";
        try {
            ProjetoDetalheDto projeto = projetoApiService.buscarPorId(id);
            model.addAttribute("projeto", projeto);
            model.addAttribute("edicao", true);
        } catch (HttpClientErrorException.Forbidden e) {
            return "redirect:/acesso-negado";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Projeto não encontrado.");
            return "redirect:/projetos";
        }
        carregarSelectsNoModel(model, session);
        model.addAttribute("menuAtivo", "projetos");
        return "projetos/form";
    }

    @PostMapping("/editar/{id}")
    public String salvarEdicao(@PathVariable Long id,
                               @RequestParam String titulo,
                               @RequestParam(required = false) String tipo,
                               @RequestParam(required = false) Long orientadorId,
                               @RequestParam(required = false) String descricao,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (!isCoordenadorOuProfessor(session)) return "redirect:/acesso-negado";
        try {
            Map<String, Object> body = buildBody(titulo, tipo, orientadorId, descricao, session);
            projetoApiService.atualizar(id, body);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Projeto atualizado com sucesso!");
            return "redirect:/projetos/" + id;
        } catch (HttpClientErrorException ex) {
            redirectAttributes.addFlashAttribute("formError",
                    extrairMensagem(ex.getResponseBodyAsString(), "Erro ao atualizar."));
            preservarFormulario(redirectAttributes, titulo, tipo, orientadorId, descricao);
            return "redirect:/projetos/editar/" + id;
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("formError", "Erro inesperado ao atualizar.");
            preservarFormulario(redirectAttributes, titulo, tipo, orientadorId, descricao);
            return "redirect:/projetos/editar/" + id;
        }
    }

    // ---- Inativar / Reativar ----

    @PostMapping("/{id}/inativar")
    public String inativar(@PathVariable Long id, HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (!isCoordenadorOuProfessor(session)) return "redirect:/acesso-negado";
        try {
            projetoApiService.inativar(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Projeto inativado com sucesso!");
        } catch (HttpClientErrorException ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    extrairMensagem(ex.getResponseBodyAsString(), "Erro ao inativar."));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao inativar.");
        }
        return "redirect:/projetos";
    }

    @PostMapping("/{id}/reativar")
    public String reativar(@PathVariable Long id, HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (!isCoordenadorOuProfessor(session)) return "redirect:/acesso-negado";
        try {
            projetoApiService.reativar(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Projeto reativado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao reativar.");
        }
        return "redirect:/projetos";
    }

    // ---- Detalhe + gerência de participantes ----

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, HttpSession session, Model model,
                          @RequestParam(required = false) String buscaParticipante,
                          RedirectAttributes redirectAttributes) {
        try {
            ProjetoDetalheDto projeto = projetoApiService.buscarPorId(id);
            model.addAttribute("projeto", projeto);

            boolean podeGerenciar = podeGerenciar(session);
            model.addAttribute("podeGerenciar", podeGerenciar);

            if (podeGerenciar && buscaParticipante != null && !buscaParticipante.isBlank()) {
                try {
                    List<Map<String, Object>> disponiveis =
                            usuarioApiService.listarParticipantesDisponiveis(buscaParticipante);
                    model.addAttribute("participantesDisponiveis", disponiveis);
                } catch (Exception e) {
                    model.addAttribute("participantesDisponiveis", List.of());
                }
            } else {
                model.addAttribute("participantesDisponiveis", List.of());
            }
        } catch (HttpClientErrorException.Forbidden e) {
            return "redirect:/acesso-negado";
        } catch (HttpClientErrorException.NotFound e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Projeto não encontrado.");
            return "redirect:/projetos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao carregar projeto.");
            return "redirect:/projetos";
        }

        model.addAttribute("menuAtivo", "projetos");
        model.addAttribute("isCoordenadorOuProfessor", isCoordenadorOuProfessor(session));
        model.addAttribute("buscaParticipante", buscaParticipante);
        return "projetos/detalhe";
    }

    @PostMapping("/{id}/participantes")
    public String adicionarParticipante(@PathVariable Long id,
                                        @RequestParam Long usuarioId,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        if (!isCoordenadorOuProfessor(session)) return "redirect:/acesso-negado";
        try {
            projetoApiService.adicionarParticipante(id, usuarioId);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Participante adicionado com sucesso!");
        } catch (HttpClientErrorException ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    extrairMensagem(ex.getResponseBodyAsString(), "Erro ao adicionar participante."));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao adicionar participante.");
        }
        return "redirect:/projetos/" + id;
    }

    @PostMapping("/{id}/participantes/{usuarioId}/remover")
    public String removerParticipante(@PathVariable Long id,
                                      @PathVariable Long usuarioId,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        if (!isCoordenadorOuProfessor(session)) return "redirect:/acesso-negado";
        try {
            projetoApiService.removerParticipante(id, usuarioId);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Participante removido com sucesso!");
        } catch (HttpClientErrorException ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    extrairMensagem(ex.getResponseBodyAsString(), "Erro ao remover participante."));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao remover participante.");
        }
        return "redirect:/projetos/" + id;
    }

    // ---- Helpers ----

    private boolean isCoordenador(HttpSession session) {
        return "COORDENADOR".equals(session.getAttribute("perfil"));
    }

    private boolean isCoordenadorOuProfessor(HttpSession session) {
        String p = (String) session.getAttribute("perfil");
        return "COORDENADOR".equals(p) || "PROFESSOR".equals(p);
    }

    private boolean podeGerenciar(HttpSession session) {
        String p = (String) session.getAttribute("perfil");
        return "COORDENADOR".equals(p) || "PROFESSOR".equals(p);
    }

    private void carregarSelectsNoModel(Model model, HttpSession session) {
        if (isCoordenador(session)) {
            try {
                model.addAttribute("responsaveis", usuarioApiService.listarResponsaveis());
            } catch (Exception e) {
                model.addAttribute("responsaveis", List.of());
            }
        }
    }

    private Map<String, Object> buildBody(String titulo, String tipo, Long orientadorId,
                                           String descricao, HttpSession session) {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("titulo", titulo);
        if (tipo != null && !tipo.isBlank()) body.put("tipo", tipo);
        if (isCoordenador(session) && orientadorId != null) body.put("orientadorId", orientadorId);
        if (descricao != null && !descricao.isBlank()) body.put("descricao", descricao);
        return body;
    }

    private void preservarFormulario(RedirectAttributes redirectAttributes,
                                      String titulo, String tipo, Long orientadorId, String descricao) {
        redirectAttributes.addFlashAttribute("formTitulo", titulo);
        redirectAttributes.addFlashAttribute("formTipo", tipo);
        redirectAttributes.addFlashAttribute("formOrientadorId", orientadorId);
        redirectAttributes.addFlashAttribute("formDescricao", descricao);
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
