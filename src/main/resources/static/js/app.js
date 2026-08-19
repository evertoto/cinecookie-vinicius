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
    const chips = [...document.querySelectorAll('.filter-chip')];
    const cards = [...document.querySelectorAll('.movie-card')];
    const count = document.querySelector('#movie-count');
    const empty = document.querySelector('#movie-empty');
    let activeFilter = 'all';

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

    search?.addEventListener('input', applyMovieFilters);
    chips.forEach((chip) => {
        chip.addEventListener('click', () => {
            activeFilter = chip.dataset.filter || 'all';
            chips.forEach((item) => item.classList.toggle('is-active', item === chip));
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

    document.querySelectorAll('[data-demo-form]').forEach((form) => {
        form.addEventListener('submit', (event) => {
            event.preventDefault();
            if (!form.checkValidity()) {
                form.reportValidity();
                return;
            }
            const status = form.querySelector('.form-status');
            if (status) {
                status.textContent = form.dataset.success;
                status.hidden = false;
            }
        });
    });

    document.querySelectorAll('[data-current-year]').forEach((node) => {
        node.textContent = String(new Date().getFullYear());
    });
});
