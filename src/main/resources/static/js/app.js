document.addEventListener('DOMContentLoaded', () => {
    const menuToggle = document.querySelector('.menu-toggle');
    const navigation = document.querySelector('.main-nav');

    menuToggle?.addEventListener('click', () => {
        const isOpen = menuToggle.getAttribute('aria-expanded') === 'true';
        menuToggle.setAttribute('aria-expanded', String(!isOpen));
        navigation?.classList.toggle('is-open', !isOpen);
        document.body.classList.toggle('menu-open', !isOpen);
    });

    navigation?.querySelectorAll('a').forEach((link) => {
        link.addEventListener('click', () => {
            menuToggle?.setAttribute('aria-expanded', 'false');
            navigation.classList.remove('is-open');
            document.body.classList.remove('menu-open');
        });
    });

    const search = document.querySelector('#movie-search');
    const searchValue = search ? localStorage.getItem('movieSearch') || '' : '';
    if (search) search.value = searchValue;
    const chips = [...document.querySelectorAll('.filter-chip')];
    const cards = [...document.querySelectorAll('.catalog-movie-card')];
    const count = document.querySelector('#movie-count');
    const empty = document.querySelector('#movie-empty');
    let activeFilter = (localStorage.getItem('activeFilter') || 'all');

    if (!chips.some((chip) => chip.dataset.filter === activeFilter)) {
        activeFilter = 'all';
    }
    chips.forEach((chip) => chip.classList.toggle('is-active', chip.dataset.filter === activeFilter));

    const normalize = (value) => value
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLowerCase();

    const applyMovieFilters = () => {
        const query = normalize(search?.value.trim() || '');
        let visible = 0;

        cards.forEach((card) => {
            const matchesCategory = activeFilter === 'all' || card.dataset.category === activeFilter;
            const matchesSearch = normalize(card.dataset.title || '').includes(query);
            const show = matchesCategory && matchesSearch;
            card.hidden = !show;
            if (show) visible += 1;
        });

        if (count) count.textContent = String(visible);
        if (empty) empty.hidden = visible !== 0;
    };

    search?.addEventListener('input', () => {
        if (search) localStorage.setItem('movieSearch', search.value);
        applyMovieFilters();
    });
    chips.forEach((chip) => {
        chip.addEventListener('click', () => {
            activeFilter = chip.dataset.filter || 'all';
            chips.forEach((item) => item.classList.toggle('is-active', item === chip));
            localStorage.setItem('activeFilter', activeFilter);
            applyMovieFilters();
        });
    });

    document.querySelectorAll('[data-password-toggle]').forEach((button) => {
        button.addEventListener('click', () => {
            const input = document.getElementById(button.dataset.passwordToggle);
            if (!input) return;
            const reveal = input.type === 'password';
            input.type = reveal ? 'text' : 'password';
            button.textContent = reveal ? 'Ocultar' : 'Mostrar';
            button.setAttribute('aria-label', reveal ? 'Ocultar senha' : 'Mostrar senha');
        });
    });

    document.querySelectorAll('[data-placeholder-link]').forEach((link) => {
        link.addEventListener('click', (event) => event.preventDefault());
    });

    document.querySelectorAll('[data-current-year]').forEach((node) => {
        node.textContent = String(new Date().getFullYear());
    });
});
