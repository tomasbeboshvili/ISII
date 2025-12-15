const state = {
    token: localStorage.getItem('authToken') || null,
    beers: [],
    currentBeerPage: 1,
    beersPerPage: 15,
    tastings: [],
    currentTastingPage: 1,
    tastingsPerPage: 10,
    tastingMode: 'mine' // 'mine' or 'beer'
};

const sessionStatus = document.getElementById('sessionStatus');
const logoutBtn = document.getElementById('logoutBtn');
const messagesBox = document.getElementById('messages');
const modalOverlay = document.getElementById('modalOverlay');
const createBeerModal = document.getElementById('createBeerModal');
const rateBeerModal = document.getElementById('rateBeerModal');
const createTastingModal = document.getElementById('createTastingModal');
const filterTastingModal = document.getElementById('filterTastingModal');

function setToken(token) {
    state.token = token;
    if (token) {
        localStorage.setItem('authToken', token);
    } else {
        localStorage.removeItem('authToken');
    }
    if (sessionStatus) {
        sessionStatus.textContent = token ? 'Sesión activa' : 'Sin sesión';
    }
}

setToken(state.token);

function showMessage(text, type = 'success') {
    if (!messagesBox) {
        console[type === 'error' ? 'error' : 'log'](text);
        return;
    }
    const div = document.createElement('div');
    div.className = `message ${type}`;
    div.textContent = text;
    messagesBox.prepend(div);
    setTimeout(() => div.remove(), 6000);
}

async function apiRequest(path, options = {}) {
    const headers = options.headers || {};
    if (!(options.body instanceof FormData)) {
        headers['Content-Type'] = 'application/json';
    }
    if (state.token) {
        headers['X-Auth-Token'] = state.token;
    }
    const res = await fetch(`/api${path}`, {
        method: 'GET',
        ...options,
        headers
    });
    let data = null;
    const text = await res.text();
    if (text) {
        try {
            data = JSON.parse(text);
        } catch (_) {
            data = text;
        }
    }
    if (!res.ok) {
        const message = data?.error || data?.message || res.statusText;
        throw new Error(message);
    }
    return data;
}

function serializeForm(form) {
    const formData = new FormData(form);
    const payload = {};
    for (const [key, value] of formData.entries()) {
        if (value !== '') {
            payload[key] = value;
        }
    }
    return payload;
}

// Botón logout (opcional por página)
if (logoutBtn) {
    logoutBtn.addEventListener('click', async () => {
        if (!state.token) {
            showMessage('No hay sesión activa', 'error');
            return;
        }
        try {
            await apiRequest('/auth/logout', {method: 'POST'});
        } catch (_) {
            // ignorar fallos de red
        }
        setToken(null);
        showMessage('Sesión cerrada');
    });
}

// Registro
// Registro
const registerForm = document.getElementById('registerForm');
if (registerForm) {
    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            const payload = serializeForm(e.target);
            if (payload.birthDate && !isAdult(payload.birthDate)) {
                showMessage('Debes ser mayor de edad para registrarte.', 'error');
                return;
            }
            if (!payload.displayName || !payload.displayName.trim()) {
                const composed = `${payload.firstName || ''} ${payload.lastName || ''}`.trim();
                payload.displayName = composed || payload.username || payload.email;
            }
            payload.birthDate = payload.birthDate || null;
            await apiRequest('/auth/register', {
                method: 'POST',
                body: JSON.stringify(payload)
            });
            showMessage('Registro correcto. Redirigiendo a inicio de sesión...');
            e.target.reset();
            setTimeout(() => {
                window.location.href = 'auth.html';
            }, 2000);
        } catch (err) {
            showMessage(err.message, 'error');
        }
    });
}

// Login
const loginForm = document.getElementById('loginForm');
if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            const payload = serializeForm(e.target);
            const data = await apiRequest('/auth/login', {
                method: 'POST',
                body: JSON.stringify(payload)
            });
            setToken(data.token);
            showMessage('Inicio de sesión correcto');
        } catch (err) {
            showMessage(err.message, 'error');
        }
    });
}

// Recuperación
// Recuperación
const recoverForm = document.getElementById('recoverForm');
if (recoverForm) {
    recoverForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('recoverEmail')?.value.trim();
        if (!email) return;
        try {
            const data = await apiRequest('/auth/password/recover', {
                method: 'POST',
                body: JSON.stringify({email})
            });
            showMessage(data.message + (data.resetToken ? ` Token: ${data.resetToken}` : ''));
            // Optionally redirect to reset page if token is provided in dev mode, or just stay
            if (data.resetToken) {
                 setTimeout(() => {
                    window.location.href = 'reset.html';
                }, 3000);
            }
        } catch (err) {
            showMessage(err.message, 'error');
        }
    });
}

const resetForm = document.getElementById('resetForm');
if (resetForm) {
    resetForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            const payload = serializeForm(resetForm);
            await apiRequest('/auth/password/reset', {
                method: 'POST',
                body: JSON.stringify(payload)
            });
            showMessage('Contraseña actualizada. Redirigiendo a inicio de sesión...');
            resetForm.reset();
            setTimeout(() => {
                window.location.href = 'auth.html';
            }, 2000);
        } catch (err) {
            showMessage(err.message, 'error');
        }
    });
}
// Perfil
const profileForm = document.getElementById('profileForm');
if (profileForm) {
    profileForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            const payload = serializeForm(e.target);
            if (!payload.displayName || !payload.displayName.trim()) {
                const composed = `${payload.firstName || ''} ${payload.lastName || ''}`.trim();
                if (composed) {
                    payload.displayName = composed;
                }
            }
            const data = await apiRequest('/users/me', {
                method: 'PUT',
                body: JSON.stringify(payload)
            });
            renderProfileCard(data);
            showMessage('Perfil actualizado');
        } catch (err) {
            showMessage(err.message, 'error');
        }
    });
}

// Cervezas
const beerForm = document.getElementById('beerForm');
if (beerForm) {
    beerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            const payload = serializeForm(e.target);
            if (payload.abv) payload.abv = parseFloat(payload.abv);
            if (payload.ibu) payload.ibu = parseInt(payload.ibu, 10);
            const data = await apiRequest('/beers', {
                method: 'POST',
                body: JSON.stringify(payload)
            });
            showMessage(`Cerveza creada con id ${data.id}`);
            e.target.reset();
            closeModal(createBeerModal);
            await loadBeers();
        } catch (err) {
            showMessage(err.message, 'error');
        }
    });
}

async function loadBeers() {
    try {
        const beers = await apiRequest('/beers');
        state.beers = beers;
        renderCatalogList();
    } catch (err) {
        showMessage(err.message, 'error');
    }
}

async function loadMyTastings() {
    try {
        const tastings = await apiRequest('/tastings/me');
        state.tastings = tastings;
        state.currentTastingPage = 1;
        updateTastingHeading('Mis degustaciones');
        renderTastingCatalog();
    } catch (err) {
        showMessage(err.message, 'error');
    }
}

async function loadTastingsByBeer(beerId) {
    try {
        const tastings = await apiRequest(`/tastings/beer/${beerId}`);
        state.tastings = tastings;
        state.currentTastingPage = 1;
        updateTastingHeading(`Degustaciones de la cerveza #${beerId}`);
        renderTastingCatalog();
    } catch (err) {
        showMessage(err.message, 'error');
    }
}

const listBeersBtn = document.getElementById('listBeersBtn');
if (listBeersBtn) {
    listBeersBtn.addEventListener('click', loadBeers);
}

const prevPageBtn = document.getElementById('prevPageBtn');
const nextPageBtn = document.getElementById('nextPageBtn');
const pageInfo = document.getElementById('pageInfo');
if (prevPageBtn) {
    prevPageBtn.addEventListener('click', () => changeBeerPage(-1));
}
if (nextPageBtn) {
    nextPageBtn.addEventListener('click', () => changeBeerPage(1));
}

const openCreateBeerBtn = document.getElementById('openCreateBeerBtn');
if (openCreateBeerBtn) {
    openCreateBeerBtn.addEventListener('click', () => openModal(createBeerModal));
}

document.querySelectorAll('[data-close]').forEach(btn => {
    btn.addEventListener('click', (event) => {
        const targetId = event.currentTarget.getAttribute('data-close');
        closeModal(document.getElementById(targetId));
    });
});

if (modalOverlay) {
    modalOverlay.addEventListener('click', (event) => {
        if (event.target === modalOverlay) {
            closeAllModals();
        }
    });
}

const ratingForm = document.getElementById('ratingForm');
if (ratingForm) {
    ratingForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            const payload = serializeForm(e.target);
            if (!payload.beerId) {
                showMessage('Selecciona una cerveza antes de valorar.', 'error');
                return;
            }
            payload.beerId = Number(payload.beerId);
            payload.score = Number(payload.score);
            if (payload.score < 1 || payload.score > 10) {
                showMessage('La puntuación debe estar entre 1 y 10.', 'error');
                return;
            }
            const data = await apiRequest('/beers/rate', {
                method: 'POST',
                body: JSON.stringify(payload)
            });
            const media = data.averageScore != null ? Number(data.averageScore).toFixed(2) : 'N/A';
            showMessage(`Valoración guardada. Media actual: ${media}`);
            ratingForm.reset();
            setText('ratingSelectedBeer', 'Ninguna');
            const beerIdInput = document.getElementById('ratingBeerId');
            if (beerIdInput) beerIdInput.value = '';
            closeModal(rateBeerModal);
            await loadBeers();
        } catch (err) {
            showMessage(err.message, 'error');
        }
    });
}

const beerListElement = document.getElementById('beerList');
if (beerListElement) {
    beerListElement.addEventListener('click', async (event) => {
        const button = event.target.closest('[data-action]');
        if (!button) {
            return;
        }
        const action = button.dataset.action;
        const id = Number(button.dataset.id);
        const name = button.dataset.name;
        if (action === 'rate-beer') {
            openRateModal(id, name);
        } else if (action === 'delete-beer') {
            const confirmDelete = window.confirm(`¿Seguro que deseas eliminar "${name}"?`);
            if (!confirmDelete) return;
            try {
                await apiRequest(`/beers/${id}`, {method: 'DELETE'});
                showMessage(`Cerveza "${name}" eliminada`);
                await loadBeers();
            } catch (err) {
                showMessage(err.message, 'error');
            }
        }
    });
}

const beerSearchInput = document.getElementById('beerSearchInput');
if (beerSearchInput) {
    beerSearchInput.addEventListener('input', renderCatalogList);
}

const refreshMenuBtn = document.getElementById('refreshMenuBtn');
if (refreshMenuBtn) {
    refreshMenuBtn.addEventListener('click', loadMenu);
}

const tastingSearchInput = document.getElementById('tastingSearchInput');
if (tastingSearchInput) {
    tastingSearchInput.addEventListener('input', renderTastingCatalog);
}

const openCreateTastingBtn = document.getElementById('openCreateTastingBtn');
if (openCreateTastingBtn) {
    openCreateTastingBtn.addEventListener('click', () => openModal(createTastingModal));
}

const openFilterTastingBtn = document.getElementById('openFilterTastingBtn');
if (openFilterTastingBtn) {
    openFilterTastingBtn.addEventListener('click', () => openModal(filterTastingModal));
}

const loadMyTastingsBtn = document.getElementById('loadMyTastingsBtn');
if (loadMyTastingsBtn) {
    loadMyTastingsBtn.addEventListener('click', () => {
        state.tastingMode = 'mine';
        loadMyTastings();
    });
}

const filterTastingForm = document.getElementById('filterTastingForm');
if (filterTastingForm) {
    filterTastingForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const hidden = document.getElementById('filterBeerId');
        let beerId = Number(hidden?.value);
        if (!beerId && filterBeerInput) {
            const fallback = resolveBeerIdFromInput(filterBeerInput.value);
            if (fallback) {
                beerId = fallback;
                if (hidden) hidden.value = fallback;
            }
        }
        if (!beerId) {
            showMessage('Selecciona una cerveza válida en el desplegable.', 'error');
            return;
        }
        state.tastingMode = 'beer';
        await loadTastingsByBeer(beerId);
        closeModal(filterTastingModal);
        if (filterBeerInput) filterBeerInput.value = '';
        if (filterBeerDropdown) filterBeerDropdown.classList.add('hidden');
        if (hidden) hidden.value = '';
    });
}

const prevTastingPageBtn = document.getElementById('prevTastingPageBtn');
const nextTastingPageBtn = document.getElementById('nextTastingPageBtn');
const tastingPageInfo = document.getElementById('tastingPageInfo');
if (prevTastingPageBtn) {
    prevTastingPageBtn.addEventListener('click', () => changeTastingPage(-1));
}
if (nextTastingPageBtn) {
    nextTastingPageBtn.addEventListener('click', () => changeTastingPage(1));
}

// Degustaciones
const tastingForm = document.getElementById('tastingForm');
if (tastingForm) {
    tastingForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            const payload = serializeForm(e.target);
            let beerId = Number(payload.beerId);
            if (!beerId && tastingBeerInput) {
                const fallback = resolveBeerIdFromInput(tastingBeerInput.value);
                if (fallback) {
                    beerId = fallback;
                    if (tastingBeerIdInput) tastingBeerIdInput.value = fallback;
                }
            }
            if (!beerId) {
                showMessage('Selecciona una cerveza válida del desplegable.', 'error');
                return;
            }
            payload.beerId = beerId;
            payload.aromaScore = Number(payload.aromaScore);
            payload.flavorScore = Number(payload.flavorScore);
            payload.appearanceScore = Number(payload.appearanceScore);
            if (payload.tastingDate) {
                payload.tastingDate = new Date(payload.tastingDate).toISOString();
            }
            const data = await apiRequest('/tastings', {
                method: 'POST',
                body: JSON.stringify(payload)
            });
            showMessage(`Degustación registrada con id ${data.id}`);
            e.target.reset();
            closeModal(createTastingModal);
            if (tastingBeerInput) tastingBeerInput.value = '';
            if (tastingBeerDropdown) tastingBeerDropdown.classList.add('hidden');
            if (tastingBeerIdInput) tastingBeerIdInput.value = '';
            if (state.tastingMode === 'mine') {
                await loadMyTastings();
            }
        } catch (err) {
            showMessage(err.message, 'error');
        }
    });
}

const toggleProfileFormBtn = document.getElementById('toggleProfileFormBtn');
if (toggleProfileFormBtn) {
    const profileFormEl = document.getElementById('profileForm');
    toggleProfileFormBtn.addEventListener('click', () => {
        profileFormEl.classList.toggle('hidden');
        toggleProfileFormBtn.textContent = profileFormEl.classList.contains('hidden')
                ? 'Editar perfil'
                : 'Ocultar formulario';
    });
}

function renderCatalogList() {
    const list = document.getElementById('beerList');
    if (!list) return;
    const term = (document.getElementById('beerSearchInput')?.value || '').toLowerCase().trim();
    const filtered = term
            ? state.beers.filter(beer => beer.name.toLowerCase().includes(term))
            : state.beers;
    const totalPages = Math.max(1, Math.ceil(filtered.length / state.beersPerPage));
    if (state.currentBeerPage > totalPages) {
        state.currentBeerPage = totalPages;
    }
    const start = (state.currentBeerPage - 1) * state.beersPerPage;
    const pageItems = filtered.slice(start, start + state.beersPerPage);
    list.innerHTML = '';
    if (!pageItems.length) {
        const li = document.createElement('li');
        li.textContent = 'No hay cervezas registradas.';
        list.appendChild(li);
        updatePagination(filtered.length, totalPages);
        return;
    }
    pageItems.forEach(beer => {
        const avg = beer.averageScore != null ? Number(beer.averageScore).toFixed(2) : 'N/A';
        const li = document.createElement('li');
        li.innerHTML = `<div class="beer-list-item">
            <div>
                <strong>${beer.name}</strong> (#${beer.id})<br>
                Estilo: ${beer.style} · ABV: ${beer.abv ?? '-'} · Media: ${avg} (${beer.ratingsCount} votos)
            </div>
            <div class="beer-actions">
                <button type="button" data-action="rate-beer" data-id="${beer.id}" data-name="${beer.name}">Valorar</button>
                <button type="button" class="danger" data-action="delete-beer" data-id="${beer.id}" data-name="${beer.name}">Eliminar</button>
            </div>
        </div>`;
        list.appendChild(li);
    });
    updatePagination(filtered.length, totalPages);
}

function renderTastingCatalog() {
    const list = document.getElementById('tastingList');
    if (!list) return;
    const term = (tastingSearchInput?.value || '').toLowerCase().trim();
    const filtered = term
            ? state.tastings.filter(t => (t.beerName || '').toLowerCase().includes(term))
            : state.tastings;
    const totalPages = Math.max(1, Math.ceil(filtered.length / state.tastingsPerPage));
    if (state.currentTastingPage > totalPages) {
        state.currentTastingPage = totalPages;
    }
    const start = (state.currentTastingPage - 1) * state.tastingsPerPage;
    const pageItems = filtered.slice(start, start + state.tastingsPerPage);
    list.innerHTML = '';
    if (!pageItems.length) {
        const li = document.createElement('li');
        li.textContent = 'No hay degustaciones para mostrar.';
        list.appendChild(li);
        updateTastingPagination(totalPages);
        return;
    }
    pageItems.forEach(tasting => {
        const li = document.createElement('li');
        li.innerHTML = `<div class="tasting-list-item">
            <div>
                <strong>${tasting.beerName ?? ('Cerveza #' + tasting.beerId)}</strong><br>
                Fecha: ${tasting.tastingDate ?? 'N/A'} · Ubicación: ${tasting.location ?? 'N/A'}<br>
                Aroma/Sabor/Apariencia: ${tasting.aromaScore}/${tasting.flavorScore}/${tasting.appearanceScore}<br>
                Notas: ${tasting.notes ?? '-'}
            </div>
        </div>`;
        list.appendChild(li);
    });
    updateTastingPagination(totalPages);
}

function openModal(modal) {
    if (!modalOverlay || !modal) return;
    modalOverlay.classList.remove('hidden');
    modal.classList.remove('hidden');
}

function closeModal(modal) {
    if (!modal) return;
    modal.classList.add('hidden');
    if (!document.querySelector('.modal:not(.hidden)')) {
        modalOverlay?.classList.add('hidden');
    }
}

function closeAllModals() {
    document.querySelectorAll('.modal').forEach(modal => modal.classList.add('hidden'));
    modalOverlay?.classList.add('hidden');
}

function updatePagination(totalItems, totalPages) {
    if (!pageInfo || !prevPageBtn || !nextPageBtn) return;
    pageInfo.textContent = `Página ${state.currentBeerPage} de ${totalPages}`;
    prevPageBtn.disabled = state.currentBeerPage === 1;
    nextPageBtn.disabled = state.currentBeerPage === totalPages;
}

function changeBeerPage(delta) {
    state.currentBeerPage += delta;
    if (state.currentBeerPage < 1) state.currentBeerPage = 1;
    renderCatalogList();
}

function renderBeerDropdown(input, dropdown, hiddenField) {
    if (!dropdown) return;
    const term = (input.value || '').toLowerCase().trim();
    const items = state.beers
            .filter(beer => beer.name.toLowerCase().includes(term))
            .slice(0, 15);
    dropdown.innerHTML = '';
    if (!items.length) {
        dropdown.classList.add('hidden');
        return;
    }
    items.forEach(beer => {
        const button = document.createElement('button');
        button.type = 'button';
        button.textContent = `${beer.name} (#${beer.id})`;
        button.addEventListener('click', () => {
            input.value = beer.name;
            hiddenField.value = beer.id;
            dropdown.classList.add('hidden');
        });
        dropdown.appendChild(button);
    });
    dropdown.classList.remove('hidden');
}

    document.addEventListener('click', (event) => {
    [tastingBeerDropdown, filterBeerDropdown].forEach(dropdown => {
        if (dropdown && !dropdown.contains(event.target) &&
                event.target !== tastingBeerInput && event.target !== filterBeerInput) {
            dropdown.classList.add('hidden');
        }
    });
});

function resolveBeerIdFromInput(value) {
    const term = (value || '').toLowerCase().trim();
    if (!term) {
        return null;
    }
    const exact = state.beers.find(beer => beer.name.toLowerCase() === term);
    if (exact) {
        return exact.id;
    }
    const partial = state.beers.find(beer => beer.name.toLowerCase().includes(term));
    return partial ? partial.id : null;
}

const tastingHeadingEl = document.getElementById('tastingHeading');
function updateTastingHeading(text) {
    if (tastingHeadingEl) tastingHeadingEl.textContent = text;
}

function updateTastingPagination(totalPages) {
    if (!tastingPageInfo || !prevTastingPageBtn || !nextTastingPageBtn) return;
    tastingPageInfo.textContent = `Página ${state.currentTastingPage} de ${totalPages}`;
    prevTastingPageBtn.disabled = state.currentTastingPage === 1;
    nextTastingPageBtn.disabled = state.currentTastingPage === totalPages;
}

function changeTastingPage(delta) {
    state.currentTastingPage += delta;
    if (state.currentTastingPage < 1) state.currentTastingPage = 1;
    renderTastingCatalog();
}

// Toggle registro en vista de auth
const toggleRegisterBtn = document.getElementById('toggleRegisterBtn');
if (toggleRegisterBtn) {
    const registerSection = document.getElementById('registerSection');
    toggleRegisterBtn.addEventListener('click', () => {
        if (!registerSection) return;
        registerSection.classList.toggle('hidden');
        toggleRegisterBtn.textContent = registerSection.classList.contains('hidden')
                ? 'Crear una ahora'
                : 'Ocultar formulario';
    });
}

// Cargar datos iniciales específicos
if (document.getElementById('beerList') || document.getElementById('tastingBeerInput') || document.getElementById('filterBeerInput')) {
    loadBeers();
}

if (document.getElementById('tastingList')) {
    loadMyTastings();
}

if (document.getElementById('profileCard') && state.token) {
    fetchProfile();
}

if (document.getElementById('menuCard')) {
    loadMenu();
}

if (document.getElementById('achievementList')) {
    loadAchievements();
}

async function fetchProfile() {
    try {
        const profile = await apiRequest('/users/me');
        renderProfileCard(profile);
    } catch (err) {
        showMessage(err.message, 'error');
    }
}

function renderProfileCard(profile) {
    const card = document.getElementById('profileCard');
    if (!card) return;
    card.classList.remove('hidden');
    setText('profileDisplayName', profile.displayName);
    setText('profileUsername', `@${profile.username}`);
    setText('profileEmail', profile.email);
    setText('profileIntro', profile.intro);
    setText('profileFirstName', profile.firstName);
    setText('profileLastName', profile.lastName);
    setText('profileOrigin', profile.origin);
    setText('profileLocation', profile.location);
    setText('profileGender', profile.gender || 'Prefiero no decirlo');
    setText('profileCity', profile.city);
    setText('profileCountry', profile.country);
    setText('profileBirthDate', profile.birthDate ?? '');
    setText('profileBio', profile.bio);
    setText('profilePoints', profile.gamificationPoints);
    setText('profileAchievement', profile.currentAchievementId ?? 'N/A');
    const photo = document.getElementById('profilePhoto');
    if (photo) {
        if (profile.photoUrl) {
            photo.src = profile.photoUrl;
            photo.style.display = 'block';
        } else {
            photo.style.display = 'none';
        }
    }
}

async function loadMenu() {
    if (!document.getElementById('menuCard')) return;
    try {
        const data = await apiRequest('/menu');
        renderMenu(data);
    } catch (err) {
        showMessage(err.message, 'error');
    }
}

function renderMenu(menu) {
    setText('menuVerified', menu.emailVerified ? 'Sí' : 'No');
    setText('menuProfile', menu.profileCompleted ? 'Completo' : 'Incompleto');
    setText('menuBeer', menu.canCreateBeer ? 'Sí' : 'No');
    setText('menuTasting', menu.canCreateTasting ? 'Sí' : 'No');
    const list = document.getElementById('menuActions');
    if (list) {
        list.innerHTML = '';
        (menu.availableActions || []).forEach(action => {
            const li = document.createElement('li');
            li.textContent = action;
            list.appendChild(li);
        });
    }
}

async function loadAchievements() {
    const container = document.getElementById('achievementList');
    if (!container) return;
    if (!state.token) {
        container.innerHTML = '<p class="muted">Inicia sesión para ver tus logros personales.</p>';
        return;
    }
    try {
        const all = await apiRequest('/achievements');
        const mine = await apiRequest('/achievements/user');
        const profile = await apiRequest('/users/me');
        const unlockedIds = new Set((mine || []).map(a => a.achievementId));
        const snapshot = buildAchievementSnapshot(profile);
        container.innerHTML = '';
        all.forEach(ach => {
            const alreadyClaimed = unlockedIds.has(ach.id);
            const details = describeAchievement(ach, snapshot);
            const percent = details.threshold > 0
                ? Math.min(100, Math.round((details.currentValue / details.threshold) * 100))
                : (alreadyClaimed ? 100 : 0);
            const unlocked = percent >= 100;
            const progressText = details.threshold > 0
                ? `${Math.min(details.currentValue, details.threshold)} / ${details.threshold}`
                : (unlocked ? 'Completado' : '0 / --');
            const card = document.createElement('div');
            card.className = `achievement-card ${unlocked ? 'unlocked' : ''}`;
            card.innerHTML = `
                <div class="achievement-meta">
                    <span class="badge-level">Nivel ${ach.level}</span>
                    <span class="achievement-topic">${ach.topic || details.criteria}</span>
                </div>
                <h3>${ach.name}</h3>
                <p class="achievement-desc">${details.description}</p>
                <div class="progress-track">
                    <div class="progress-bar ${unlocked ? 'completed' : ''}" style="width:${percent}%;"></div>
                </div>
                <div class="progress-meta">
                    <span>${progressText}</span>
                    <span class="status-pill ${unlocked ? 'ok' : 'pending'}">${unlocked ? 'Desbloqueado' : 'En progreso'}</span>
                </div>
            `;
            container.appendChild(card);
        });
    } catch (err) {
        showMessage(err.message, 'error');
    }
}

function openRateModal(beerId, beerName) {
    const hidden = document.getElementById('ratingBeerId');
    if (hidden) hidden.value = beerId;
    setText('ratingSelectedBeer', `${beerName} (#${beerId})`);
    openModal(rateBeerModal);
}

function isAdult(dateStr) {
    const birth = new Date(dateStr);
    const today = new Date();
    const age = today.getFullYear() - birth.getFullYear();
    const m = today.getMonth() - birth.getMonth();
    const effectiveAge = m < 0 || (m === 0 && today.getDate() < birth.getDate()) ? age - 1 : age;
    return effectiveAge >= 18;
}

function setText(id, text = '') {
    const el = document.getElementById(id);
    if (el) el.textContent = text ?? '';
}

function buildAchievementSnapshot(profile) {
    return {
        GAMIFICATION: profile?.gamificationPoints ?? 0,
        BEERS_CREATED: profile?.beersCreatedCount ?? 0,
        TASTINGS: profile?.tastingsCount ?? 0,
        RATINGS: profile?.ratingsCount ?? 0
    };
}

function describeAchievement(achievement, snapshot) {
    const threshold = achievement.threshold ?? 0;
    const criteria = (achievement.criteria || 'GAMIFICATION').toUpperCase();
    const messages = {
        GAMIFICATION: {
            verb: 'Suma',
            noun: 'puntos de gamificación',
            current: snapshot.GAMIFICATION ?? 0
        },
        BEERS_CREATED: {
            verb: 'Publica',
            noun: 'cervezas nuevas',
            current: snapshot.BEERS_CREATED ?? 0
        },
        TASTINGS: {
            verb: 'Realiza',
            noun: 'degustaciones',
            current: snapshot.TASTINGS ?? 0
        },
        RATINGS: {
            verb: 'Valora',
            noun: 'cervezas',
            current: snapshot.RATINGS ?? 0
        }
    };
    const data = messages[criteria] || {
        verb: 'Completa',
        noun: `el hito ${criteria.toLowerCase()}`,
        current: snapshot.GAMIFICATION ?? 0
    };
    const description = threshold > 0
            ? `${data.verb} ${threshold} ${data.noun}.`
            : `Consigue este galardón especial participando en la comunidad.`;
    return {
        description,
        currentValue: data.current ?? 0,
        threshold,
        criteria
    };
}

const tastingBeerInput = document.getElementById('tastingBeerInput');
const tastingBeerDropdown = document.getElementById('tastingBeerDropdown');
const tastingBeerIdInput = document.getElementById('tastingBeerId');
if (tastingBeerInput && tastingBeerDropdown && tastingBeerIdInput) {
    tastingBeerInput.addEventListener('input', () => renderBeerDropdown(tastingBeerInput, tastingBeerDropdown, tastingBeerIdInput));
}

const filterBeerInput = document.getElementById('filterBeerInput');
const filterBeerDropdown = document.getElementById('filterBeerDropdown');
const filterBeerIdInput = document.getElementById('filterBeerId');
if (filterBeerInput && filterBeerDropdown && filterBeerIdInput) {
    filterBeerInput.addEventListener('input', () => renderBeerDropdown(filterBeerInput, filterBeerDropdown, filterBeerIdInput));
}

// --- Friends & Feed Logic ---

function checkAuth() {
    if (!state.token) {
        window.location.href = 'auth.html';
    }
    buildNav();
}

function buildNav() {
    const nav = document.getElementById('main-nav');
    if (!nav) return;
    nav.innerHTML = `
        <a href="index.html">Inicio</a>
        <a href="beers.html">Cervezas</a>
        <a href="tastings.html">Degustaciones</a>
        <a href="achievements.html">Logros</a>
        <a href="friends.html">Amigos</a>
        <a href="feed.html">Actividad</a>
        <a href="statistics.html">Estadísticas</a>
        <a href="profile.html">Mi Perfil</a>
        <button id="logoutBtnNav" style="margin-left: auto;">Cerrar Sesión</button>
    `;
    document.getElementById('logoutBtnNav').addEventListener('click', () => {
        setToken(null);
        window.location.href = 'index.html';
    });
}

async function searchUsers() {
    const input = document.getElementById('user-search-input');
    const resultsDiv = document.getElementById('search-results');
    if (!input || !resultsDiv) return;
    
    const query = input.value.trim();
    if (!query) return;

    try {
        const [users, friends] = await Promise.all([
            apiRequest(`/friends/search?q=${encodeURIComponent(query)}`),
            apiRequest('/friends')
        ]);
        
        const friendIds = new Set(friends.map(f => f.id));

        resultsDiv.innerHTML = '';
        if (users.length === 0) {
            resultsDiv.innerHTML = '<p>No se encontraron usuarios.</p>';
            return;
        }
        
        users.forEach(user => {
            const isFriend = friendIds.has(user.id);
            const div = document.createElement('div');
            div.className = 'card';
            
            let actionBtn = `<button onclick="sendRequest(${user.id})">Enviar Solicitud</button>`;
            if (isFriend) {
                actionBtn = `<button class="success" disabled>Amigo</button>`;
            }

            div.innerHTML = `
                <h4>${user.displayName} (@${user.username})</h4>
                ${actionBtn}
            `;
            resultsDiv.appendChild(div);
        });
    } catch (err) {
        showMessage(err.message, 'error');
    }
}

async function sendRequest(userId) {
    try {
        await apiRequest(`/friends/request/${userId}`, { method: 'POST' });
        showMessage('Solicitud enviada.');
        loadFriendsData();
    } catch (err) {
        showMessage(err.message, 'error');
    }
}

async function loadFriendsData() {
    const pendingDiv = document.getElementById('requests-list');
    const friendsDiv = document.getElementById('friends-list');
    if (!pendingDiv || !friendsDiv) return;

    try {
        // Pending
        const pending = await apiRequest('/friends/pending');
        pendingDiv.innerHTML = '';
        if (pending.length === 0) {
            pendingDiv.innerHTML = '<p>No tienes solicitudes pendientes.</p>';
        } else {
            pending.forEach(req => {
                const div = document.createElement('div');
                div.className = 'card';
                div.innerHTML = `
                    <h4>Solicitud de @${req.requester.username}</h4>
                    <div class="actions">
                        <button onclick="resolveRequest(${req.id}, true)">Aceptar</button>
                        <button class="danger" onclick="resolveRequest(${req.id}, false)">Rechazar</button>
                    </div>
                `;
                pendingDiv.appendChild(div);
            });
        }

        // Friends
        const friends = await apiRequest('/friends');
        friendsDiv.innerHTML = '';
        if (friends.length === 0) {
            friendsDiv.innerHTML = '<p>Aún no tienes amigos.</p>';
        } else {
            friends.forEach(friend => {
                const div = document.createElement('div');
                div.className = 'card';
                div.innerHTML = `
                    <h4>${friend.displayName} (@${friend.username})</h4>
                    <button class="danger" onclick="removeFriend(${friend.id})">Eliminar</button>
                `;
                friendsDiv.appendChild(div);
            });
        }
    } catch (err) {
        showMessage(err.message, 'error');
    }
}

async function resolveRequest(requestId, accept) {
    try {
        await apiRequest(`/friends/request/${requestId}/resolve`, {
            method: 'POST',
            body: JSON.stringify({ accept })
        });
        showMessage(accept ? 'Solicitud aceptada.' : 'Solicitud rechazada.');
        loadFriendsData();
    } catch (err) {
        showMessage(err.message, 'error');
    }
}

async function removeFriend(friendId) {
    if (!confirm('¿Seguro que quieres eliminar a este amigo?')) return;
    try {
        await apiRequest(`/friends/${friendId}`, { method: 'DELETE' });
        showMessage('Amigo eliminado.');
        loadFriendsData();
    } catch (err) {
        showMessage(err.message, 'error');
    }
}

async function loadActivityFeed() {
    const feedDiv = document.getElementById('feed-list');
    if (!feedDiv) return;

    try {
        const items = await apiRequest('/activity/feed');
        feedDiv.innerHTML = '';
        if (items.length === 0) {
            feedDiv.innerHTML = '<p>No hay actividad reciente de tus amigos.</p>';
            return;
        }

        items.forEach(item => {
            const div = document.createElement('div');
            div.className = 'feed-item card';
            const date = new Date(item.timestamp).toLocaleString();
            
            let content = '';
            if (item.type === 'TASTING') {
                const t = item.details;
                content = `
                    <p><strong>@${item.username}</strong> degustó <strong>${item.beerName}</strong></p>
                    <p>Puntuación: A:${t.aromaScore} / S:${t.flavorScore} / Ap:${t.appearanceScore}</p>
                    ${t.notes ? `<p><em>"${t.notes}"</em></p>` : ''}
                `;
            } else if (item.type === 'RATING') {
                const r = item.details;
                content = `
                    <p><strong>@${item.username}</strong> valoró <strong>${item.beerName}</strong></p>
                    <p>Nota: <strong>${r.score}/10</strong></p>
                    ${r.comment ? `<p><em>"${r.comment}"</em></p>` : ''}
                `;
            }

            div.innerHTML = `
                <div class="feed-header">
                    <span class="date">${date}</span>
                </div>
                <div class="feed-content">
                    ${content}
                </div>
            `;
            feedDiv.appendChild(div);
        });
    } catch (err) {
        showMessage(err.message, 'error');
    }
}

async function loadStatistics() {
    const totalBeersRated = document.getElementById('totalBeersRated');
    if (!totalBeersRated) return;

    try {
        const stats = await apiRequest('/statistics/me');
        
        setText('totalBeersRated', stats.totalBeersRated);
        setText('totalTastings', stats.totalTastings);
        setText('averageBeerRating', stats.averageBeerRating);
        setText('averageTastingRating', stats.averageTastingRating);

    } catch (err) {
        showMessage(err.message, 'error');
    }
}

function highlightActiveNav() {
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.nav-links a');
    navLinks.forEach(link => {
        if (link.getAttribute('href') === currentPath.substring(1) || (currentPath === '/' && link.getAttribute('href') === 'index.html')) {
            link.classList.add('active');
        }
    });
}

document.addEventListener('DOMContentLoaded', highlightActiveNav);

// --- Dark Mode Logic ---
function initDarkMode() {
    // Initial check is now handled by inline script in head to prevent FOUC
    // Just handle the toggle button here

    // Create toggle button
    const nav = document.querySelector('.nav-links');
    if (nav) {
        const btn = document.createElement('button');
        btn.className = 'theme-toggle';
        // Simple SVG for Sun/Moon (Half-filled circle representation)
        btn.innerHTML = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M12 3a9 9 0 1 0 9 9c0-.46-.04-.92-.1-1.36a5.389 5.389 0 0 1-4.4 2.26 5.403 5.403 0 0 1-3.14-9.8c-.44-.06-.9-.1-1.36-.1z"/></svg>`;
        btn.title = 'Cambiar tema';
        btn.addEventListener('click', () => {
            document.documentElement.classList.toggle('dark-mode');
            const isDark = document.documentElement.classList.contains('dark-mode');
            localStorage.setItem('theme', isDark ? 'dark' : 'light');
        });
        nav.appendChild(btn);
    }
}

// Initialize dark mode immediately to prevent flash if possible, 
// but DOM elements like nav need to exist.
// Since this script is at the end of body, DOM is ready.
initDarkMode();
