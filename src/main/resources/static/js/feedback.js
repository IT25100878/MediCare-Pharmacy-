document.addEventListener("DOMContentLoaded", () => {
    const message = document.getElementById("message");
    const messageCount = document.getElementById("messageCount");
    const rating = document.getElementById("rating");
    const ratingPreview = document.getElementById("ratingPreview");
    const revealItems = document.querySelectorAll("[data-reveal]");
    const reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    const ratingMessages = {
        5: "Excellent — thank you for sharing what is working well.",
        4: "Good — tell us what would make it even better.",
        3: "Average — your suggestion can help improve the experience.",
        2: "Needs improvement — please include the issue details.",
        1: "Poor — please describe the problem so Admin can review it."
    };

    document.body.classList.add("motion-ready");
    revealItems.forEach((item) => item.style.setProperty("--reveal-delay", `${item.dataset.revealDelay || 0}ms`));
    if (reducedMotion || !("IntersectionObserver" in window)) {
        revealItems.forEach((item) => item.classList.add("revealed"));
    } else {
        const revealObserver = new IntersectionObserver((entries, observer) => {
            entries.forEach((entry) => {
                if (entry.isIntersecting) {
                    entry.target.classList.add("revealed");
                    observer.unobserve(entry.target);
                }
            });
        }, { threshold: 0.12 });
        revealItems.forEach((item) => revealObserver.observe(item));
    }

    const updateMessageCount = () => {
        if (message && messageCount) {
            messageCount.textContent = `${message.value.length} / 2000`;
        }
    };

    const updateRatingPreview = () => {
        if (!rating || !ratingPreview) {
            return;
        }
        const value = Number(rating.value);
        const stars = value ? "★".repeat(value) + "☆".repeat(5 - value) : "☆☆☆☆☆";
        ratingPreview.querySelector("span").textContent = stars;
        ratingPreview.querySelector("p").textContent = ratingMessages[value] || "Choose a rating to share your experience.";
        ratingPreview.classList.toggle("active", Boolean(value));
    };

    message?.addEventListener("input", updateMessageCount);
    rating?.addEventListener("change", updateRatingPreview);
    updateMessageCount();
    updateRatingPreview();
});
