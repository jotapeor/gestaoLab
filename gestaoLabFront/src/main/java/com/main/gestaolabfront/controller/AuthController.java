package com.main.gestaolabfront.controller;

import com.main.gestaolabfront.dto.LoginResponse;
import com.main.gestaolabfront.dto.UsuarioDto;
import com.main.gestaolabfront.service.AuthApiService;
import com.main.gestaolabfront.service.ProjetoApiService;
import com.main.gestaolabfront.service.UsuarioApiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final AuthApiService authApiService;
    private final UsuarioApiService usuarioApiService;
    private final ProjetoApiService projetoApiService;

    public AuthController(AuthApiService authApiService, UsuarioApiService usuarioApiService,
                          ProjetoApiService projetoApiService) {
        this.authApiService = authApiService;
        this.usuarioApiService = usuarioApiService;
        this.projetoApiService = projetoApiService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @PostMapping("/logar")
    public String logar(@RequestParam String email,
                        @RequestParam String senha,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        try {
            LoginResponse response = authApiService.logar(email, senha);
            session.setAttribute("token", response.token());
            session.setAttribute("nome", response.nome());
            session.setAttribute("perfil", response.perfil());
            session.setAttribute("primeiroAcesso", String.valueOf(response.primeiroAcesso()));
            return "redirect:/dashboard";
        } catch (HttpClientErrorException ex) {
            String msg = extrairMensagemErro(ex.getResponseBodyAsString(), "Credenciais inválidas.");
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/login";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Não foi possível conectar ao servidor.");
            return "redirect:/login";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        model.addAttribute("nome", session.getAttribute("nome"));
        model.addAttribute("perfil", session.getAttribute("perfil"));
        model.addAttribute("menuAtivo", "dashboard");
        return "comum/dashboard";
    }

    @GetMapping("/trocar-senha")
    public String trocarSenhaPage() {
        return "auth/trocar-senha";
    }

    @PostMapping("/fazer-trocar-senha")
    public String trocarSenha(@RequestParam String senhaAtual,
                              @RequestParam String novaSenha,
                              @RequestParam String confirmacaoSenha,
                              HttpSession session,
                              Model model) {
        if (!novaSenha.equals(confirmacaoSenha)) {
            model.addAttribute("errorMessage", "As senhas não conferem.");
            return "auth/trocar-senha";
        }
        try {
            LoginResponse response = authApiService.trocarSenha(senhaAtual, novaSenha);
            session.setAttribute("token", response.token());
            session.setAttribute("nome", response.nome());
            session.setAttribute("perfil", response.perfil());
            session.setAttribute("primeiroAcesso", "false");
            return "redirect:/dashboard";
        } catch (HttpClientErrorException ex) {
            String msg = extrairMensagemErro(ex.getResponseBodyAsString(), "Erro ao trocar senha.");
            model.addAttribute("errorMessage", msg);
            return "auth/trocar-senha";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/meu-perfil")
    public String meuPerfil(HttpSession session, Model model) {
        try {
            UsuarioDto usuario = usuarioApiService.me();
            model.addAttribute("usuario", usuario);
        } catch (Exception e) {
            model.addAttribute("usuario", new UsuarioDto(null, null, null, null, null, null, null, null, null, null));
        }
        try {
            model.addAttribute("meusProjetos", projetoApiService.meusProjetos());
        } catch (Exception e) {
            model.addAttribute("meusProjetos", java.util.List.of());
        }
        model.addAttribute("menuAtivo", "");
        model.addAttribute("urlVoltar", "/dashboard");
        return "usuarios/meu-perfil";
    }

    @PostMapping("/meu-perfil")
    public String salvarMeuPerfil(@RequestParam String nome,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        try {
            UsuarioDto atualizado = usuarioApiService.atualizarMe(nome);
            session.setAttribute("nome", atualizado.nome());
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Perfil atualizado com sucesso!");
        } catch (HttpClientErrorException ex) {
            redirectAttributes.addFlashAttribute("formError",
                    extrairMensagemErro(ex.getResponseBodyAsString(), "Erro ao atualizar perfil."));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("formError", "Erro inesperado ao salvar.");
        }
        return "redirect:/meu-perfil";
    }

    private String extrairMensagemErro(String responseBody, String fallback) {
        if (responseBody == null || responseBody.isBlank()) return fallback;
        int idx = responseBody.indexOf("\"message\":\"");
        if (idx == -1) return fallback;
        int start = idx + "\"message\":\"".length();
        int end = responseBody.indexOf("\"", start);
        return end == -1 ? fallback : responseBody.substring(start, end);
    }
}
