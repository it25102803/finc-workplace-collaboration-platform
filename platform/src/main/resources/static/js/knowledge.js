const knowledgeList = document.getElementById("knowledgeList");
const knowledgeStatus = document.getElementById("knowledgeStatus");

async function loadKnowledge() {
    knowledgeStatus.textContent = "Loading knowledge items...";
    knowledgeList.replaceChildren();

    try {
        const response = await fetch("/api/knowledge");
        if (!response.ok) {
            throw new Error(`Request failed (${response.status})`);
        }

        const items = await response.json();
        if (!items.length) {
            knowledgeStatus.textContent = "";
            const empty = document.createElement("div");
            empty.className = "empty-state";
            empty.textContent = "No knowledge items have been added yet.";
            knowledgeList.append(empty);
            return;
        }

        knowledgeStatus.textContent = `${items.length} knowledge ${items.length === 1 ? "item" : "items"}`;
        items.forEach(item => {
            const article = document.createElement("article");
            article.className = "article";

            const title = document.createElement("h2");
            title.textContent = item.title || "Untitled";
            article.append(title);

            const content = document.createElement("p");
            content.textContent = item.content || "";
            article.append(content);

            const metadata = document.createElement("footer");
            const category = document.createElement("span");
            category.textContent = item.category || "General";
            const author = document.createElement("span");
            author.textContent = item.createdBy ? `Added by ${item.createdBy}` : "";
            metadata.append(category, author);
            article.append(metadata);
            knowledgeList.append(article);
        });
    } catch (error) {
        console.error("Unable to load knowledge items", error);
        knowledgeStatus.textContent = "Knowledge items could not be loaded. Check the server and try again.";
    }
}

document.getElementById("refreshKnowledge").addEventListener("click", loadKnowledge);
loadKnowledge();
