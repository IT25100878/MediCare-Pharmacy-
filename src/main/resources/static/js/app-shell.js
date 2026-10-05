if (!window.mediCareSidebarInitialized) {
    window.mediCareSidebarInitialized = true;
    document.addEventListener("DOMContentLoaded", () => {
        const sidebar = document.getElementById("appSidebar");
        const toggle = document.getElementById("appSidebarToggle");
        const backdrop = document.getElementById("appSidebarBackdrop");
        const currentPath = window.location.pathname.replace(/\/$/, "") || "/";

        const closeSidebar = () => {
            sidebar?.classList.remove("is-open");
            toggle?.classList.remove("is-open");
            toggle?.setAttribute("aria-expanded", "false");
            backdrop?.classList.remove("is-open");
            if (backdrop) backdrop.hidden = true;
            document.body.classList.remove("app-sidebar-open");
        };

        const openSidebar = () => {
            sidebar?.classList.add("is-open");
            toggle?.classList.add("is-open");
            toggle?.setAttribute("aria-expanded", "true");
            if (backdrop) {
                backdrop.hidden = false;
                backdrop.classList.add("is-open");
            }
            document.body.classList.add("app-sidebar-open");
        };

        toggle?.addEventListener("click", () => {
            if (sidebar?.classList.contains("is-open")) closeSidebar();
            else openSidebar();
        });
        backdrop?.addEventListener("click", closeSidebar);
        document.addEventListener("keydown", (event) => {
            if (event.key === "Escape") closeSidebar();
        });

        sidebar?.querySelectorAll(".app-nav-link").forEach((link) => {
            const url = new URL(link.href, window.location.origin);
            const targetPath = url.pathname.replace(/\/$/, "") || "/";
            if (targetPath === currentPath) {
                link.classList.add("is-active");
                link.setAttribute("aria-current", "page");
            }
            link.addEventListener("click", closeSidebar);
        });
    });
}
