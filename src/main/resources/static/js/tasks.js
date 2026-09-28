let allTasks = [];
let currentFilter = "ALL";


document.addEventListener("DOMContentLoaded", () => {

    loadTasks();

    const form = document.getElementById("taskForm");

    form.addEventListener("submit", function (event) {
        event.preventDefault();
        saveTask();
    });

});


/* =========================
   LOAD TASKS
========================= */

async function loadTasks() {

    try {

        const response = await fetch("/api/tasks");

        if (!response.ok) {
            throw new Error("Unable to load tasks");
        }

        allTasks = await response.json();

        updateStatistics();
        renderTasks();

    } catch (error) {

        console.error(error);

        showBoardMessage(
            "Unable to load tasks. Please make sure the Spring Boot application and database are running."
        );
    }
}


/* =========================
   STATISTICS
========================= */

function updateStatistics() {

    document.getElementById("totalTasks").textContent =
        allTasks.length;

    document.getElementById("todoTasks").textContent =
        allTasks.filter(task => task.status === "TODO").length;

    document.getElementById("progressTasks").textContent =
        allTasks.filter(task => task.status === "IN_PROGRESS").length;

    document.getElementById("completedTasks").textContent =
        allTasks.filter(task => task.status === "COMPLETED").length;
}


/* =========================
   RENDER TASKS
========================= */

function renderTasks() {

    const todoColumn =
        document.getElementById("todoColumn");

    const progressColumn =
        document.getElementById("progressColumn");

    const completedColumn =
        document.getElementById("completedColumn");

    const cancelledColumn =
        document.getElementById("cancelledColumn");

    todoColumn.innerHTML = "";
    progressColumn.innerHTML = "";
    completedColumn.innerHTML = "";
    cancelledColumn.innerHTML = "";


    const search =
        document.getElementById("taskSearch")
            .value
            .toLowerCase()
            .trim();


    let filteredTasks = allTasks.filter(task => {

        const matchesFilter =
            currentFilter === "ALL" ||
            task.status === currentFilter;

        const matchesSearch =
            !search ||
            (task.title || "")
                .toLowerCase()
                .includes(search) ||
            (task.description || "")
                .toLowerCase()
                .includes(search);

        return matchesFilter && matchesSearch;
    });


    const todoTasks =
        filteredTasks.filter(task => task.status === "TODO");

    const progressTasks =
        filteredTasks.filter(task => task.status === "IN_PROGRESS");

    const completedTasks =
        filteredTasks.filter(task => task.status === "COMPLETED");

    const cancelledTasks =
        filteredTasks.filter(task => task.status === "CANCELLED");


    renderColumn(todoColumn, todoTasks);

    renderColumn(progressColumn, progressTasks);

    renderColumn(completedColumn, completedTasks);

    renderColumn(cancelledColumn, cancelledTasks);


    document.getElementById("todoCount").textContent =
        todoTasks.length;

    document.getElementById("progressCount").textContent =
        progressTasks.length;

    document.getElementById("completedCount").textContent =
        completedTasks.length;

    document.getElementById("cancelledCount").textContent =
        cancelledTasks.length;
}


/* =========================
   RENDER COLUMN
========================= */

function renderColumn(container, tasks) {

    if (tasks.length === 0) {

        container.innerHTML = `
            <div class="empty-state">
                No tasks here
            </div>
        `;

        return;
    }


    tasks.forEach(task => {

        container.appendChild(
            createTaskCard(task)
        );

    });
}


/* =========================
   TASK CARD
========================= */

function createTaskCard(task) {

    const card =
        document.createElement("div");

    card.className = "task-card";


    const progress =
        Number(task.progress || 0);


    let assignedUser = "Unassigned";

    if (task.assignedUser) {

        assignedUser =
            task.assignedUser.username ||
            (
                (task.assignedUser.firstName || "") +
                " " +
                (task.assignedUser.lastName || "")
            ).trim() ||
            "User #" + task.assignedUser.userId;
    }


    card.innerHTML = `

        <h4>${escapeHtml(task.title || "Untitled Task")}</h4>

        <div class="task-description">
            ${escapeHtml(task.description || "No description")}
        </div>

        <div class="task-meta">

            <span class="priority priority-${task.priority}">
                ${formatPriority(task.priority)}
            </span>

            <span class="task-date">
                ${formatDate(task.dueDate)}
            </span>

        </div>


        <div class="progress-area">

            <div class="progress-info">

                <span>Progress</span>

                <span>${progress}%</span>

            </div>

            <div class="progress-bar">

                <div
                    class="progress-fill"
                    style="width:${progress}%">
                </div>

            </div>

        </div>


        <div class="assigned-user">
            Assigned to: ${escapeHtml(assignedUser)}
        </div>


        <div class="task-actions">

            <button
                class="task-action"
                onclick="editTask(${task.taskId})">
                Edit
            </button>

            <button
                class="task-action"
                onclick="changeProgress(${task.taskId}, ${progress})">
                Progress
            </button>

            <button
                class="task-action delete"
                onclick="deleteTask(${task.taskId})">
                Delete
            </button>

        </div>

    `;


    return card;
}


/* =========================
   FILTER
========================= */

function filterTasks(status, button) {

    currentFilter = status;

    document
        .querySelectorAll(".filter-btn")
        .forEach(btn => btn.classList.remove("active"));

    button.classList.add("active");

    renderTasks();
}


/* =========================
   OPEN MODAL
========================= */

function openTaskModal() {

    document.getElementById("taskId").value = "";

    document.getElementById("modalTitle").textContent =
        "Create New Task";

    document.getElementById("taskForm").reset();

    document.getElementById("priority").value = "MEDIUM";

    document.getElementById("status").value = "TODO";

    document.getElementById("progress").value = 0;

    updateProgressLabel();

    clearFormMessage();

    document
        .getElementById("taskModal")
        .classList.add("show");
}


/* =========================
   CLOSE MODAL
========================= */

function closeTaskModal() {

    document
        .getElementById("taskModal")
        .classList.remove("show");
}


/* =========================
   SAVE TASK
========================= */

async function saveTask() {

    clearFormMessage();


    const id =
        document.getElementById("taskId").value;


    const assignedUserIdValue =
        document.getElementById("assignedUserId").value;


    const task = {

        title:
            document.getElementById("title").value.trim(),

        description:
            document.getElementById("description").value.trim(),

        priority:
        document.getElementById("priority").value,

        status:
        document.getElementById("status").value,

        progress:
            Number(document.getElementById("progress").value),

        startDate:
            document.getElementById("startDate").value || null,

        dueDate:
            document.getElementById("dueDate").value || null
    };


    if (!task.title) {

        showFormMessage("Task title is required.");

        return;
    }


    let url = "/api/tasks";

    let method = "POST";


    if (id) {

        url = "/api/tasks/" + id;

        method = "PUT";
    }


    if (assignedUserIdValue) {

        url += "?assignedUserId=" +
            encodeURIComponent(assignedUserIdValue);
    }


    try {

        const response = await fetch(url, {

            method: method,

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify(task)
        });


        if (!response.ok) {

            const errorText =
                await response.text();

            throw new Error(
                errorText || "Unable to save task"
            );
        }


        closeTaskModal();

        await loadTasks();


    } catch (error) {

        console.error(error);

        showFormMessage(
            "Unable to save task: " + error.message
        );
    }
}


/* =========================
   EDIT TASK
========================= */

async function editTask(id) {

    try {

        const response =
            await fetch("/api/tasks/" + id);


        if (!response.ok) {
            throw new Error("Task not found");
        }


        const task =
            await response.json();


        document.getElementById("taskId").value =
            task.taskId;

        document.getElementById("modalTitle").textContent =
            "Edit Task";

        document.getElementById("title").value =
            task.title || "";

        document.getElementById("description").value =
            task.description || "";

        document.getElementById("priority").value =
            task.priority || "MEDIUM";

        document.getElementById("status").value =
            task.status || "TODO";

        document.getElementById("progress").value =
            task.progress || 0;

        document.getElementById("startDate").value =
            task.startDate || "";

        document.getElementById("dueDate").value =
            task.dueDate || "";


        if (task.assignedUser) {

            document.getElementById("assignedUserId").value =
                task.assignedUser.userId || "";
        } else {

            document.getElementById("assignedUserId").value =
                "";
        }


        updateProgressLabel();

        clearFormMessage();

        document
            .getElementById("taskModal")
            .classList.add("show");


    } catch (error) {

        alert("Unable to load task.");
    }
}


/* =========================
   DELETE TASK
========================= */

async function deleteTask(id) {

    const confirmed =
        confirm(
            "Are you sure you want to delete this task?"
        );


    if (!confirmed) {
        return;
    }


    try {

        const response =
            await fetch(
                "/api/tasks/" + id,
                {
                    method: "DELETE"
                }
            );


        if (!response.ok) {
            throw new Error("Delete failed");
        }


        await loadTasks();


    } catch (error) {

        alert(
            "Unable to delete task."
        );
    }
}


/* =========================
   UPDATE PROGRESS
========================= */

async function changeProgress(id, currentProgress) {

    const value =
        prompt(
            "Enter progress (0 - 100):",
            currentProgress
        );


    if (value === null) {
        return;
    }


    const progress =
        Number(value);


    if (
        Number.isNaN(progress) ||
        progress < 0 ||
        progress > 100
    ) {

        alert(
            "Progress must be between 0 and 100."
        );

        return;
    }


    try {

        const response =
            await fetch(
                `/api/tasks/${id}/progress?progress=${progress}`,
                {
                    method: "PATCH"
                }
            );


        if (!response.ok) {
            throw new Error("Progress update failed");
        }


        await loadTasks();


    } catch (error) {

        alert(
            "Unable to update progress."
        );
    }
}


/* =========================
   PROGRESS LABEL
========================= */

function updateProgressLabel() {

    const value =
        document.getElementById("progress").value;

    document.getElementById("progressValue")
        .textContent = value;
}


/* =========================
   FORM MESSAGE
========================= */

function showFormMessage(message) {

    const element =
        document.getElementById("formMessage");

    element.textContent = message;

    element.classList.add("show");
}


function clearFormMessage() {

    const element =
        document.getElementById("formMessage");

    element.textContent = "";

    element.classList.remove("show");
}


function showBoardMessage(message) {

    document.getElementById("todoColumn").innerHTML =
        `<div class="empty-state">${message}</div>`;
}


/* =========================
   HELPERS
========================= */

function formatPriority(priority) {

    if (!priority) {
        return "Medium";
    }

    return priority.charAt(0) +
        priority.slice(1).toLowerCase();
}


function formatDate(date) {

    if (!date) {
        return "No due date";
    }

    const parts =
        date.split("-");

    if (parts.length !== 3) {
        return date;
    }

    return `${parts[2]}/${parts[1]}/${parts[0]}`;
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
   CLOSE MODAL OUTSIDE
========================= */

document.addEventListener("click", function (event) {

    const modal =
        document.getElementById("taskModal");

    if (event.target === modal) {
        closeTaskModal();
    }

});