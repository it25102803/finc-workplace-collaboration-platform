let currentFolderId = null;
let breadcrumbStack = [];
let currentFolders = [];
let currentFiles = [];
let uploadMode = "new-file";
let uploadTargetFileId = null;


document.addEventListener("DOMContentLoaded", () => {

    const savedUserId = localStorage.getItem("filesUserId");
    if (savedUserId) {
        document.getElementById("actingUserId").value = savedUserId;
    }

    loadCurrentLevel();

    document.getElementById("folderForm").addEventListener("submit", function (event) {
        event.preventDefault();
        saveFolder();
    });

    document.getElementById("uploadForm").addEventListener("submit", function (event) {
        event.preventDefault();
        submitUpload();
    });

});


/* =========================
   USER ID
========================= */

function onUserIdChanged() {
    const value = document.getElementById("actingUserId").value;
    localStorage.setItem("filesUserId", value);
}

function getUserId() {
    const value = Number(document.getElementById("actingUserId").value);
    if (!value) {
        alert("Enter your user ID first (top of the page).");
        return null;
    }
    return value;
}


/* =========================
   LOAD CURRENT LEVEL
========================= */

async function loadCurrentLevel() {

    const ownerId = document.getElementById("actingUserId").value;

    try {

        let folders = [];

        if (currentFolderId === null) {

            if (!ownerId) {
                currentFolders = [];
                currentFiles = [];
                renderAll();
                return;
            }

            const response = await fetch("/api/folders/roots?ownerId=" + encodeURIComponent(ownerId));
            if (!response.ok) throw new Error("Unable to load folders");
            folders = await response.json();
            currentFiles = [];

        } else {

            const [folderRes, filesRes] = await Promise.all([
                fetch("/api/folders/" + currentFolderId + "/subfolders"),
                fetch("/api/folders/" + currentFolderId + "/contents")
            ]);

            if (!folderRes.ok || !filesRes.ok) throw new Error("Unable to load folder contents");

            folders = await folderRes.json();
            currentFiles = await filesRes.json();
        }

        currentFolders = folders;
        renderAll();

    } catch (error) {

        console.error(error);
        document.getElementById("folderGrid").innerHTML =
            `<div class="empty-state">Unable to load. Make sure the Spring Boot app is running.</div>`;
    }
}


/* =========================
   RENDER
========================= */

function renderAll() {

    renderBreadcrumb();
    renderFolders();
    renderFiles();
    renderStats();
}


function renderBreadcrumb() {

    const container = document.getElementById("breadcrumb");
    container.innerHTML = "";

    const home = document.createElement("span");
    home.className = "crumb" + (currentFolderId === null ? " active" : "");
    home.textContent = "Home";
    home.onclick = goToRoot;
    container.appendChild(home);

    breadcrumbStack.forEach((crumb, index) => {

        const sep = document.createElement("span");
        sep.className = "crumb-sep";
        sep.textContent = "/";
        container.appendChild(sep);

        const item = document.createElement("span");
        const isLast = index === breadcrumbStack.length - 1;
        item.className = "crumb" + (isLast ? " active" : "");
        item.textContent = crumb.name;
        item.onclick = () => goToBreadcrumb(index);
        container.appendChild(item);
    });

    document.getElementById("currentLocation").textContent =
        breadcrumbStack.length ? breadcrumbStack[breadcrumbStack.length - 1].name : "Home";
}


function renderFolders() {

    const grid = document.getElementById("folderGrid");
    grid.innerHTML = "";

    if (currentFolderId === null && !document.getElementById("actingUserId").value) {
        grid.innerHTML = `<div class="empty-state">Enter your user ID above to see your folders.</div>`;
        return;
    }

    if (currentFolders.length === 0) {
        grid.innerHTML = `<div class="empty-state">No folders here yet.</div>`;
        return;
    }

    currentFolders.forEach(folder => {

        const card = document.createElement("div");
        card.className = "folder-card";

        card.innerHTML = `
            <div class="folder-card-icon">&#128193;</div>
            <div class="folder-card-name">${escapeHtml(folder.name)}</div>
            <div class="folder-card-actions">
                <button type="button" class="rename">Rename</button>
                <button type="button" class="delete">Delete</button>
            </div>
        `;

        card.addEventListener("click", () => openFolder(folder));

        card.querySelector(".rename").addEventListener("click", (event) => {
            event.stopPropagation();
            renameFolder(folder);
        });

        card.querySelector(".delete").addEventListener("click", (event) => {
            event.stopPropagation();
            deleteFolder(folder);
        });

        grid.appendChild(card);
    });
}


function renderFiles() {

    const body = document.getElementById("fileTableBody");
    const emptyState = document.getElementById("fileEmptyState");
    body.innerHTML = "";

    if (currentFolderId === null) {
        emptyState.style.display = "block";
        emptyState.textContent = "Open a folder to see its files.";
        return;
    }

    if (currentFiles.length === 0) {
        emptyState.style.display = "block";
        emptyState.textContent = "No files in this folder yet.";
        return;
    }

    emptyState.style.display = "none";

    currentFiles.forEach(file => {

        const row = document.createElement("tr");

        row.innerHTML = `
            <td class="file-name">${escapeHtml(file.fileName)}</td>
            <td>${formatFileType(file.fileType)}</td>
            <td>${formatSize(file.fileSizeBytes)}</td>
            <td>v${file.currentVersion}</td>
            <td>${formatDate(file.uploadedAt)}</td>
            <td>
                <div class="file-actions">
                    <button type="button" class="download">Download</button>
                    <button type="button" class="new-version">New version</button>
                    <button type="button" class="versions">Versions</button>
                    <button type="button" class="rename">Rename</button>
                    <button type="button" class="delete">Delete</button>
                </div>
            </td>
        `;

        row.querySelector(".download").addEventListener("click", () => downloadFile(file));
        row.querySelector(".new-version").addEventListener("click", () => openUploadModal(file.fileId));
        row.querySelector(".versions").addEventListener("click", () => openVersionsModal(file));
        row.querySelector(".rename").addEventListener("click", () => renameFile(file));
        row.querySelector(".delete").addEventListener("click", () => deleteFile(file));

        body.appendChild(row);
    });
}


function renderStats() {

    document.getElementById("folderCount").textContent = currentFolders.length;
    document.getElementById("fileCount").textContent = currentFiles.length;

    const totalBytes = currentFiles.reduce((sum, f) => sum + (f.fileSizeBytes || 0), 0);
    document.getElementById("totalSize").textContent = formatSize(totalBytes);
}


/* =========================
   NAVIGATION
========================= */

function openFolder(folder) {
    breadcrumbStack.push({ id: folder.folderId, name: folder.name });
    currentFolderId = folder.folderId;
    loadCurrentLevel();
}

function goToRoot() {
    breadcrumbStack = [];
    currentFolderId = null;
    loadCurrentLevel();
}

function goToBreadcrumb(index) {
    breadcrumbStack = breadcrumbStack.slice(0, index + 1);
    currentFolderId = breadcrumbStack[breadcrumbStack.length - 1].id;
    loadCurrentLevel();
}


/* =========================
   FOLDER MODAL
========================= */

function openFolderModal() {

    if (getUserId() === null) return;

    document.getElementById("folderForm").reset();
    clearMessage("folderFormMessage");
    document.getElementById("folderModal").classList.add("show");
}

function closeFolderModal() {
    document.getElementById("folderModal").classList.remove("show");
}

async function saveFolder() {

    clearMessage("folderFormMessage");

    const ownerId = getUserId();
    if (ownerId === null) return;

    const name = document.getElementById("folderName").value.trim();
    if (!name) {
        showMessage("folderFormMessage", "Folder name is required.");
        return;
    }

    try {

        let response;

        if (currentFolderId === null) {

            response = await fetch("/api/folders", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ name: name, ownerId: ownerId, departmentId: null })
            });

        } else {

            response = await fetch("/api/folders/" + currentFolderId + "/subfolders", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ name: name, ownerId: ownerId })
            });
        }

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || "Unable to create folder");
        }

        closeFolderModal();
        await loadCurrentLevel();

    } catch (error) {

        console.error(error);
        showMessage("folderFormMessage", "Unable to create folder: " + error.message);
    }
}


async function renameFolder(folder) {

    const name = prompt("New name for this folder:", folder.name);
    if (!name || name.trim() === "") return;

    try {

        const response = await fetch("/api/folders/" + folder.folderId + "/rename", {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name: name.trim() })
        });

        if (!response.ok) throw new Error("Rename failed");

        await loadCurrentLevel();

    } catch (error) {
        alert("Unable to rename folder.");
    }
}


async function deleteFolder(folder) {

    const confirmed = confirm(
        `Delete "${folder.name}" and everything inside it? This cannot be undone.`
    );
    if (!confirmed) return;

    try {

        const response = await fetch("/api/folders/" + folder.folderId, { method: "DELETE" });
        if (!response.ok) throw new Error("Delete failed");

        await loadCurrentLevel();

    } catch (error) {
        alert("Unable to delete folder.");
    }
}


/* =========================
   UPLOAD MODAL
========================= */

function openUploadModal(fileId) {

    if (getUserId() === null) return;

    if (fileId) {

        uploadMode = "new-version";
        uploadTargetFileId = fileId;
        document.getElementById("uploadModalTitle").textContent = "Upload new version";
        document.getElementById("fileTypeGroup").style.display = "none";
        document.getElementById("changeNoteGroup").style.display = "block";

    } else {

        if (currentFolderId === null) {
            alert("Open a folder first, then upload a file into it.");
            return;
        }

        uploadMode = "new-file";
        uploadTargetFileId = null;
        document.getElementById("uploadModalTitle").textContent = "Upload file";
        document.getElementById("fileTypeGroup").style.display = "block";
        document.getElementById("changeNoteGroup").style.display = "none";
    }

    document.getElementById("uploadForm").reset();
    clearMessage("uploadFormMessage");
    document.getElementById("uploadModal").classList.add("show");
}

function closeUploadModal() {
    document.getElementById("uploadModal").classList.remove("show");
}

async function submitUpload() {

    clearMessage("uploadFormMessage");

    const userId = getUserId();
    if (userId === null) return;

    const fileInput = document.getElementById("uploadFile");
    const file = fileInput.files[0];

    if (!file) {
        showMessage("uploadFormMessage", "Choose a file first.");
        return;
    }

    const formData = new FormData();
    formData.append("data", file);

    try {

        let response;

        if (uploadMode === "new-file") {

            formData.append("folderId", currentFolderId);
            formData.append("uploaderId", userId);
            formData.append("fileType", document.getElementById("uploadFileType").value);

            response = await fetch("/api/files/upload", { method: "POST", body: formData });

        } else {

            formData.append("createdById", userId);
            formData.append("changeNote", document.getElementById("changeNote").value || "");

            response = await fetch("/api/files/" + uploadTargetFileId + "/versions", {
                method: "POST",
                body: formData
            });
        }

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || "Upload failed");
        }

        closeUploadModal();
        await loadCurrentLevel();

    } catch (error) {

        console.error(error);
        showMessage("uploadFormMessage", "Unable to upload: " + error.message);
    }
}


/* =========================
   FILE ACTIONS
========================= */

function downloadFile(file) {
    window.open("/api/files/" + file.fileId + "/download", "_blank");
}


async function renameFile(file) {

    const name = prompt("New name for this file:", file.fileName);
    if (!name || name.trim() === "") return;

    try {

        const response = await fetch("/api/files/" + file.fileId + "/rename", {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name: name.trim() })
        });

        if (!response.ok) throw new Error("Rename failed");

        await loadCurrentLevel();

    } catch (error) {
        alert("Unable to rename file.");
    }
}


async function deleteFile(file) {

    const confirmed = confirm(`Delete "${file.fileName}"?`);
    if (!confirmed) return;

    try {

        const response = await fetch("/api/files/" + file.fileId, { method: "DELETE" });
        if (!response.ok) throw new Error("Delete failed");

        await loadCurrentLevel();

    } catch (error) {
        alert("Unable to delete file.");
    }
}


/* =========================
   VERSIONS MODAL
========================= */

async function openVersionsModal(file) {

    document.getElementById("versionsModalTitle").textContent = "Versions of " + file.fileName;
    document.getElementById("versionList").innerHTML = "Loading...";
    document.getElementById("versionsModal").classList.add("show");

    try {

        const response = await fetch("/api/files/" + file.fileId + "/versions");
        if (!response.ok) throw new Error("Unable to load versions");

        const versions = await response.json();
        renderVersionList(versions, file);

    } catch (error) {

        document.getElementById("versionList").innerHTML =
            `<div class="empty-state">Unable to load version history.</div>`;
    }
}

function closeVersionsModal() {
    document.getElementById("versionsModal").classList.remove("show");
}

function renderVersionList(versions, file) {

    const container = document.getElementById("versionList");
    container.innerHTML = "";

    if (versions.length === 0) {
        container.innerHTML = `<div class="empty-state">No versions found.</div>`;
        return;
    }

    versions.forEach(version => {

        const isCurrent = version.versionNumber === file.currentVersion;

        const row = document.createElement("div");
        row.className = "version-row" + (isCurrent ? " current" : "");

        row.innerHTML = `
            <div class="version-info">
                <strong>Version ${version.versionNumber}${isCurrent ? " (current)" : ""}</strong>
                <div>${formatSize(version.fileSizeBytes)} &middot; ${formatDate(version.createdAt)}</div>
                <div>${escapeHtml(version.changeNote || "No change note")}</div>
            </div>
        `;

        if (!isCurrent) {

            const restoreBtn = document.createElement("button");
            restoreBtn.type = "button";
            restoreBtn.textContent = "Restore";
            restoreBtn.addEventListener("click", () => restoreVersion(version, file));
            row.appendChild(restoreBtn);
        }

        container.appendChild(row);
    });
}

async function restoreVersion(version, file) {

    const confirmed = confirm(`Restore version ${version.versionNumber} of "${file.fileName}"?`);
    if (!confirmed) return;

    try {

        const response = await fetch("/api/files/versions/" + version.versionId + "/restore", {
            method: "POST"
        });

        if (!response.ok) throw new Error("Restore failed");

        closeVersionsModal();
        await loadCurrentLevel();

    } catch (error) {
        alert("Unable to restore version.");
    }
}


/* =========================
   HELPERS
========================= */

function formatSize(bytes) {

    if (!bytes || bytes <= 0) return "0 KB";
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + " KB";
    return (bytes / (1024 * 1024)).toFixed(1) + " MB";
}

function formatFileType(type) {

    if (!type) return "Document";
    return type.charAt(0) + type.slice(1).toLowerCase();
}

function formatDate(value) {

    if (!value) return "Unknown";
    const date = new Date(value);
    if (isNaN(date.getTime())) return value;
    return date.toLocaleDateString() + " " + date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
}

function showMessage(elementId, message) {
    const element = document.getElementById(elementId);
    element.textContent = message;
    element.classList.add("show");
}

function clearMessage(elementId) {
    const element = document.getElementById(elementId);
    element.textContent = "";
    element.classList.remove("show");
}

function escapeHtml(value) {
    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}


/* =========================
   CLOSE MODALS OUTSIDE
========================= */

document.addEventListener("click", function (event) {

    ["folderModal", "uploadModal", "versionsModal"].forEach(id => {
        const modal = document.getElementById(id);
        if (event.target === modal) {
            modal.classList.remove("show");
        }
    });

});
