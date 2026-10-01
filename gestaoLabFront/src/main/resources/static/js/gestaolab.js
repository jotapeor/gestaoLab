function validarCamposLogin() {
    const email = document.getElementById('email');
    const senha = document.getElementById('senha');
    const btn = document.getElementById('btn-logar');
    if (!email || !senha || !btn) return;
    btn.disabled = !(email.value.trim().length > 0 && senha.value.trim().length > 0);
    email.classList.toggle('success', email.value.trim().length > 0);
}

function toggleSenha(btnEl) {
    const wrapper = btnEl.closest('.gl-input-wrapper');
    if (!wrapper) return;
    const input = wrapper.querySelector('input');
    const icon = btnEl.querySelector('i');
    if (input.type === 'password') {
        input.type = 'text';
        if (icon) icon.classList.replace('bi-eye', 'bi-eye-slash');
    } else {
        input.type = 'password';
        if (icon) icon.classList.replace('bi-eye-slash', 'bi-eye');
    }
}

function initSidebar() {
    const toggleBtn = document.getElementById('sidebarToggle');
    const sidebar = document.getElementById('glSidebar');
    const overlay = document.getElementById('sidebarOverlay');
    if (!toggleBtn || !sidebar) return;
    toggleBtn.addEventListener('click', () => {
        sidebar.classList.toggle('open');
        if (overlay) overlay.classList.toggle('active');
    });
    if (overlay) {
        overlay.addEventListener('click', () => {
            sidebar.classList.remove('open');
            overlay.classList.remove('active');
        });
    }
}

function initSubmitLoading() {
    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', function () {
            const btn = this.querySelector('[type="submit"]:not(:disabled)');
            if (btn) { btn.disabled = true; btn.textContent = 'Aguarde...'; }
        });
    });
}

function initTrocarSenhaValidation() {
    const form = document.getElementById('formTrocarSenha');
    if (!form) return;

    const novaSenha = document.getElementById('novaSenha');
    const confirmacao = document.getElementById('confirmacaoSenha');
    const btnTrocar = document.getElementById('btn-trocar');

    function validar() {
        const sv = novaSenha ? novaSenha.value : '';
        const cv = confirmacao ? confirmacao.value : '';
        const se = document.getElementById('nova-senha-error');
        const ce = document.getElementById('confirmacao-error');
        const cs = document.getElementById('confirmacao-success');
        let valido = true;

        if (sv.length > 0 && sv.length < 8) {
            if (novaSenha) { novaSenha.classList.add('error'); novaSenha.classList.remove('success'); }
            if (se) { se.textContent = 'Mínimo de 8 caracteres.'; se.style.display = 'block'; }
            valido = false;
        } else if (sv.length >= 8) {
            if (novaSenha) { novaSenha.classList.remove('error'); novaSenha.classList.add('success'); }
            if (se) se.style.display = 'none';
        } else {
            if (novaSenha) novaSenha.classList.remove('error', 'success');
            if (se) se.style.display = 'none';
            valido = false;
        }

        if (cv.length > 0) {
            if (sv === cv && sv.length >= 8) {
                if (confirmacao) { confirmacao.classList.remove('error'); confirmacao.classList.add('success'); }
                if (ce) ce.style.display = 'none';
                if (cs) { cs.textContent = 'Senhas conferem.'; cs.style.display = 'block'; }
            } else {
                if (confirmacao) { confirmacao.classList.add('error'); confirmacao.classList.remove('success'); }
                if (ce) { ce.textContent = 'Senhas não conferem.'; ce.style.display = 'block'; }
                if (cs) cs.style.display = 'none';
                valido = false;
            }
        } else {
            if (confirmacao) confirmacao.classList.remove('error', 'success');
            if (ce) ce.style.display = 'none';
            if (cs) cs.style.display = 'none';
            valido = false;
        }

        const senhaAtual = document.getElementById('senhaAtual');
        if (!senhaAtual || senhaAtual.value.trim() === '') valido = false;

        if (btnTrocar) btnTrocar.disabled = !valido;
    }

    if (novaSenha) novaSenha.addEventListener('input', validar);
    if (confirmacao) confirmacao.addEventListener('input', validar);
    const senhaAtual = document.getElementById('senhaAtual');
    if (senhaAtual) senhaAtual.addEventListener('input', validar);
}

function showToast(message, type) {
    type = type || 'success';
    let container = document.getElementById('gl-toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'gl-toast-container';
        container.className = 'gl-toast-container';
        document.body.appendChild(container);
    }
    const toast = document.createElement('div');
    toast.className = 'gl-toast gl-toast-' + type;
    toast.innerHTML = '<i class="bi bi-' + (type === 'success' ? 'check-circle' : 'exclamation-circle') + '"></i> <span>' + message + '</span>';
    container.appendChild(toast);
    setTimeout(function () {
        toast.classList.add('fade-out');
        setTimeout(function () { toast.remove(); }, 300);
    }, 3000);
}

function initToasts() {
    const successMsg = document.getElementById('toast-success-message');
    if (successMsg && successMsg.value) showToast(successMsg.value, 'success');
    const errorMsg = document.getElementById('toast-error-message');
    if (errorMsg && errorMsg.value) showToast(errorMsg.value, 'danger');
}

document.addEventListener('DOMContentLoaded', function () {
    initSidebar();
    initSubmitLoading();
    initTrocarSenhaValidation();
    initToasts();

    const emailInput = document.getElementById('email');
    const senhaInput = document.getElementById('senha');
    if (emailInput) emailInput.addEventListener('input', validarCamposLogin);
    if (senhaInput) senhaInput.addEventListener('input', validarCamposLogin);
    validarCamposLogin();
});
