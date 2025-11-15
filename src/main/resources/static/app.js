const state = {
    token: localStorage.getItem('authToken') || null
};

const tokenLabel = document.getElementById('tokenLabel');
const logoutBtn = document.getElementById('logoutBtn');
const messagesBox = document.getElementById('messages');

function setToken(token) {
    state.token = token;
    if (token) {
        localStorage.setItem('authToken', token);
    } else {
        localStorage.removeItem('authToken');
    }
    if (tokenLabel) {
        tokenLabel.textContent = token || 'sin sesión';
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
const registerForm = document.getElementById('registerForm');
if (registerForm) {
    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            const payload = serializeForm(e.target);
            if (!payload.displayName || !payload.displayName.trim()) {
                const composed = `${payload.firstName || ''} ${payload.lastName || ''}`.trim();
                payload.displayName = composed || payload.username || payload.email;
            }
            payload.birthDate = payload.birthDate || null;
            const data = await apiRequest('/auth/register', {
                method: 'POST',
                body: JSON.stringify(payload)
            });
            showMessage(`Registro correcto. Token activación: ${data.activationToken}`);
            const activationInput = document.getElementById('activationToken');
            if (activationInput) {
                activationInput.value = data.activationToken || '';
            }
            e.target.reset();
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

// Activación
const activateBtn = document.getElementById('activateBtn');
if (activateBtn) {
    activateBtn.addEventListener('click', async () => {
        const activationInput = document.getElementById('activationToken');
        const token = activationInput?.value.trim();
        if (!token) return;
        try {
            await apiRequest('/auth/activate', {
                method: 'POST',
                body: JSON.stringify({token})
            });
            showMessage('Cuenta activada');
        } catch (err) {
            showMessage(err.message, 'error');
        }
    });
}

// Recuperación
const recoverBtn = document.getElementById('recoverBtn');
if (recoverBtn) {
    recoverBtn.addEventListener('click', async () => {
        const email = document.getElementById('recoverEmail')?.value.trim();
        if (!email) return;
        try {
            const data = await apiRequest('/auth/password/recover', {
                method: 'POST',
                body: JSON.stringify({email})
            });
            showMessage(data.message + (data.resetToken ? ` Token: ${data.resetToken}` : ''));
        } catch (err) {
            showMessage(err.message, 'error');
        }
    });
}

// Perfil
const profileBtn = document.getElementById('loadProfileBtn');
if (profileBtn) {
    profileBtn.addEventListener('click', async () => {
        try {
            const profile = await apiRequest('/users/me');
            renderProfileCard(profile);
        } catch (err) {
            showMessage(err.message, 'error');
        }
    });
}

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
            await loadBeers();
        } catch (err) {
            showMessage(err.message, 'error');
        }
    });
}

async function loadBeers() {
    try {
        const beers = await apiRequest('/beers');
        const list = document.getElementById('beerList');
        list.innerHTML = '';
        beers.forEach(beer => {
            const li = document.createElement('li');
            li.innerHTML = `<strong>${beer.id} - ${beer.name}</strong><br>
                Estilo: ${beer.style} · ABV: ${beer.abv ?? '-'} · Media: ${beer.averageScore ?? 'N/A'} (${beer.ratingsCount} votos)`;
            list.appendChild(li);
        });
    } catch (err) {
        showMessage(err.message, 'error');
    }
}

const listBeersBtn = document.getElementById('listBeersBtn');
if (listBeersBtn) {
    listBeersBtn.addEventListener('click', loadBeers);
}

const ratingForm = document.getElementById('ratingForm');
if (ratingForm) {
    ratingForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            const payload = serializeForm(e.target);
            payload.beerId = Number(payload.beerId);
            payload.score = Number(payload.score);
            const data = await apiRequest('/beers/rate', {
                method: 'POST',
                body: JSON.stringify(payload)
            });
            showMessage(`Valoración guardada. Media actual: ${data.averageScore ?? 'N/A'}`);
            e.target.reset();
            await loadBeers();
        } catch (err) {
            showMessage(err.message, 'error');
        }
    });
}

// Degustaciones
const tastingForm = document.getElementById('tastingForm');
if (tastingForm) {
    tastingForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            const payload = serializeForm(e.target);
            payload.beerId = Number(payload.beerId);
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
        } catch (err) {
            showMessage(err.message, 'error');
        }
    });
}

const myTastingsBtn = document.getElementById('myTastingsBtn');
if (myTastingsBtn) {
    myTastingsBtn.addEventListener('click', async () => {
        try {
            const tastings = await apiRequest('/tastings/me');
            renderTastingList(document.getElementById('myTastingsList'), tastings);
        } catch (err) {
            showMessage(err.message, 'error');
        }
    });
}

const beerTastingsBtn = document.getElementById('beerTastingsBtn');
if (beerTastingsBtn) {
    beerTastingsBtn.addEventListener('click', async () => {
        const beerId = Number(document.getElementById('beerTastingsId')?.value);
        if (!beerId) return;
        try {
            const tastings = await apiRequest(`/tastings/beer/${beerId}`);
            renderTastingList(document.getElementById('beerTastingsList'), tastings);
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

function renderTastingList(container, tastings) {
    container.innerHTML = '';
    tastings.forEach(tasting => {
        const li = document.createElement('li');
        li.innerHTML = `<strong>#${tasting.id} · ${tasting.beerName ?? tasting.beerId}</strong><br>
        ${tasting.tastingDate ?? ''}<br>
        Aroma/Flavor/App: ${tasting.aromaScore}/${tasting.flavorScore}/${tasting.appearanceScore}`;
        container.appendChild(li);
    });
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

// Menú
const menuBtn = document.getElementById('menuBtn');
if (menuBtn) {
    menuBtn.addEventListener('click', async () => {
        try {
            const data = await apiRequest('/menu');
            const info = document.getElementById('menuInfo');
            if (info) {
                info.textContent = JSON.stringify(data, null, 2);
            }
        } catch (err) {
            showMessage(err.message, 'error');
        }
    });
}

// Galardones
const listAchievementsBtn = document.getElementById('listAchievementsBtn');
if (listAchievementsBtn) {
    listAchievementsBtn.addEventListener('click', async () => {
        try {
            const data = await apiRequest('/achievements');
            const list = document.getElementById('achievementList');
            if (!list) return;
            list.innerHTML = '';
            data.forEach(ach => {
                const li = document.createElement('li');
                li.innerHTML = `<strong>${ach.id} - ${ach.name}</strong><br>
                    Nivel: ${ach.level} · Umbral: ${ach.threshold}`;
                list.appendChild(li);
            });
        } catch (err) {
            showMessage(err.message, 'error');
        }
    });
}

const claimAchievementBtn = document.getElementById('claimAchievementBtn');
if (claimAchievementBtn) {
    claimAchievementBtn.addEventListener('click', async () => {
        const id = Number(document.getElementById('claimAchievementId')?.value);
        if (!id) return;
        try {
            await apiRequest(`/achievements/${id}/claim`, {method: 'POST'});
            showMessage('Galardón asignado');
            const input = document.getElementById('claimAchievementId');
            if (input) input.value = '';
        } catch (err) {
            showMessage(err.message, 'error');
        }
    });
}

// Cargar datos iniciales específicos
if (state.token) {
    if (document.getElementById('beerList')) {
        loadBeers();
    }
}

function renderProfileCard(profile) {
    const card = document.getElementById('profileCard');
    if (!card) return;
    card.classList.remove('hidden');
    const setText = (id, text = '') => {
        const el = document.getElementById(id);
        if (el) el.textContent = text ?? '';
    };
    setText('profileDisplayName', profile.displayName);
    setText('profileUsername', `@${profile.username}`);
    setText('profileEmail', profile.email);
    setText('profileIntro', profile.intro);
    setText('profileFirstName', profile.firstName);
    setText('profileLastName', profile.lastName);
    setText('profileCity', profile.city);
    setText('profileCountry', profile.country);
    setText('profileLocation', profile.location);
    setText('profileGender', profile.gender);
    setText('profileBirthday', profile.birthday ?? '');
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
