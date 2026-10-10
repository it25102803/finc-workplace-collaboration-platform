let allTasks = [];
let assignableUsers = [];
let currentTask = null;
let currentUserId = localStorage.getItem('finc_logged_in_user') || '1';

document.addEventListener('DOMContentLoaded', () => {
    initUserSwitcher();
    initTaskEventListeners();
    fetchUsers();
    fetchTasks();
});

function initUserSwitcher() {
    const userSelect = document.getElementById('currentUserSelect');
    userSelect.value = currentUserId;
    updateSidebarUserBadge(currentUserId);

    userSelect.addEventListener('change', (e) => {
        currentUserId = e.target.value;
        localStorage.setItem('finc_logged_in_user', currentUserId);
        updateSidebarUserBadge(currentUserId);
        fetchTasks();
    });
}

function updateSidebarUserBadge(userId) {
    const avatar = document.getElementById('sidebarAvatar');
    const name = document.getElementById('sidebarUserName');
    const role = document.getElementById('sidebarUserRole');

    if (userId === '1') {
        avatar.textContent = 'A';
        name.textContent = 'Admin';
        role.textContent = 'System Administrator';
    } else {
        avatar.textContent = `M${userId - 1}`;
        name.textContent = `Member ${userId - 1}`;
        role.textContent = 'Team Member';
    }
}

async function fetchUsers() {
    try {
        const res = await fetch('/api/tasks/users');
        if (res.ok) {
            assignableUsers = await res.json();
            populateAssigneeDropdown();
        }
    } catch (e) {
        console.warn('Could not load dynamic users, using defaults:', e);
    }
}

function populateAssigneeDropdown() {
    const select = document.getElementById('taskAssignee');
    select.innerHTML = '<option value="">👥 All Members (Visible to Everyone)</option>';
    assignableUsers.forEach(u => {
        const opt = document.createElement('option');
        opt.value = u.id;
        opt.textContent = `👤 ${u.name || u.username}`;
        select.appendChild(opt);
    });
}

function initTaskEventListeners() {
    const modal = document.getElementById('taskModal');
    const detailModal = document.getElementById('taskDetailModal');

    document.getElementById('btnOpenTaskModal').addEventListener('click', () => {
        document.getElementById('taskForm').reset();
        document.getElementById('taskId').value = '';
        document.getElementById('taskModalTitle').textContent = 'Create Task';
        document.getElementById('progressVal').textContent = '0';
        document.getElementById('taskAssignee').value = '';
        modal.classList.add('active');
    });

    document.getElementById('btnCloseTaskModal').addEventListener('click', () => modal.classList.remove('active'));
    document.getElementById('btnCancelTaskModal').addEventListener('click', () => modal.classList.remove('active'));

    document.getElementById('btnCloseDetailModal').addEventListener('click', () => detailModal.classList.remove('active'));
    document.getElementById('btnCloseDetailBtn').addEventListener('click', () => detailModal.classList.remove('active'));

    document.getElementById('taskProgress').addEventListener('input', (e) => {
        document.getElementById('progressVal').textContent = e.target.value;
    });

    document.getElementById('taskForm').addEventListener('submit', handleSaveTask);
    document.getElementById('commentForm').addEventListener('submit', handleAddComment);
    document.getElementById('btnDeleteTask').addEventListener('click', handleDeleteTask);
    document.getElementById('btnEditTask').addEventListener('click', handleOpenEditTask);
}

async function fetchTasks() {
    try {
        const res = await fetch(`/api/tasks?currentUserId=${currentUserId}`);
        if (res.ok) {
            allTasks = await res.json();
            renderKanban();
        }
    } catch (e) {
        console.error('Error fetching tasks:', e);
    }
}

function renderKanban() {
    ['TODO', 'IN_PROGRESS', 'IN_REVIEW', 'COMPLETED'].forEach(status => {
        const container = document.getElementById(`cards-${status}`);
        const countBadge = document.getElementById(`count-${status}`);
        container.innerHTML = '';

        const statusTasks = allTasks.filter(t => t.status === status);
        countBadge.textContent = statusTasks.length;

        statusTasks.forEach(task => {
            const card = document.createElement('div');
            card.className = 'task-card';

            const assigneeBadge = task.assignee
                ? `<span class="badge badge-user"><i class="fa-solid fa-user"></i> ${task.assignee.firstName || task.assignee.username}</span>`
                : `<span class="badge badge-all"><i class="fa-solid fa-users"></i> All Members</span>`;

            card.innerHTML = `
                <div class="card-title">${task.title}</div>
                <div class="progress-bar-bg">
                    <div class="progress-bar-fill" style="width: ${task.progress || 0}%"></div>
                </div>
                <div class="card-meta">
                    <span class="badge badge-${(task.priority || 'MEDIUM').toLowerCase()}">${task.priority || 'MEDIUM'}</span>
                    ${assigneeBadge}
                    <span>${task.dueDate ? '<i class="fa-regular fa-clock"></i> ' + task.dueDate : ''}</span>
                </div>
            `;
            card.addEventListener('click', () => openTaskDetails(task));
            container.appendChild(card);
        });
    });
}

async function handleSaveTask(e) {
    e.preventDefault();
    const taskId = document.getElementById('taskId').value;
    const title = document.getElementById('taskTitle').value;
    const priority = document.getElementById('taskPriority').value;
    const status = document.getElementById('taskStatus').value;
    const startDate = document.getElementById('taskStartDate').value || null;
    const dueDate = document.getElementById('taskDueDate').value || null;
    const progress = parseInt(document.getElementById('taskProgress').value, 10);
    const description = document.getElementById('taskDesc').value;
    const assignedUserId = document.getElementById('taskAssignee').value || '';

    const payload = { title, priority, status, startDate, dueDate, progress, description };

    let url = taskId
        ? `/api/tasks/${taskId}?createdByUserId=${currentUserId}`
        : `/api/tasks?createdByUserId=${currentUserId}`;

    if (assignedUserId) {
        url += `&assignedUserId=${assignedUserId}`;
    }

    try {
        const res = await fetch(url, {
            method: taskId ? 'PUT' : 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            document.getElementById('taskModal').classList.remove('active');
            fetchTasks();
        } else {
            const err = await res.text();
            console.error('Save failed:', err);
            alert('Failed to save task: ' + err);
        }
    } catch (err) {
        console.error('Error saving task:', err);
        alert('Network error while saving task.');
    }
}

function openTaskDetails(task) {
    currentTask = task;
    document.getElementById('viewTaskTitle').textContent = task.title;
    document.getElementById('viewPriority').textContent = task.priority;
    document.getElementById('viewPriority').className = `badge badge-${(task.priority || 'medium').toLowerCase()}`;
    document.getElementById('viewStatus').textContent = task.status;
    document.getElementById('viewStatus').className = 'badge';

    const assigneeText = task.assignee
        ? `👤 Assigned to: ${task.assignee.firstName || task.assignee.username}`
        : `👥 Assigned to: All Members`;
    document.getElementById('viewAssignee').textContent = assigneeText;

    document.getElementById('viewDueDate').textContent = task.dueDate || 'No due date';
    document.getElementById('viewDescription').textContent = task.description || 'No description provided.';

    loadComments(task.taskId);
    document.getElementById('taskDetailModal').classList.add('active');
}

async function loadComments(taskId) {
    const list = document.getElementById('commentList');
    list.innerHTML = '';
    try {
        const res = await fetch(`/api/tasks/${taskId}/comments`);
        if (res.ok) {
            const comments = await res.json();
            comments.forEach(c => {
                const bubble = document.createElement('div');
                bubble.className = 'comment-bubble';
                const authorName = c.author ? (c.author.firstName || c.author.username) : 'Member';
                bubble.innerHTML = `<strong>${authorName}:</strong> ${c.content}`;
                list.appendChild(bubble);
            });
        }
    } catch (e) {
        console.error('Error loading comments:', e);
    }
}

async function handleAddComment(e) {
    e.preventDefault();
    if (!currentTask) return;
    const input = document.getElementById('commentInput');
    const content = input.value;

    try {
        const res = await fetch(`/api/tasks/${currentTask.taskId}/comments?userId=${currentUserId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ content })
        });
        if (res.ok) {
            input.value = '';
            loadComments(currentTask.taskId);
        }
    } catch (e) {
        console.error('Error adding comment:', e);
    }
}

async function handleDeleteTask() {
    if (!currentTask || !confirm('Are you sure you want to delete this task?')) return;
    try {
        await fetch(`/api/tasks/${currentTask.taskId}`, { method: 'DELETE' });
        document.getElementById('taskDetailModal').classList.remove('active');
        fetchTasks();
    } catch (e) {
        console.error('Error deleting task:', e);
    }
}

function handleOpenEditTask() {
    if (!currentTask) return;
    document.getElementById('taskDetailModal').classList.remove('active');

    document.getElementById('taskId').value = currentTask.taskId;
    document.getElementById('taskTitle').value = currentTask.title;
    document.getElementById('taskPriority').value = currentTask.priority;
    document.getElementById('taskStatus').value = currentTask.status;
    document.getElementById('taskStartDate').value = currentTask.startDate || '';
    document.getElementById('taskDueDate').value = currentTask.dueDate || '';
    document.getElementById('taskProgress').value = currentTask.progress || 0;
    document.getElementById('progressVal').textContent = currentTask.progress || 0;
    document.getElementById('taskDesc').value = currentTask.description || '';
    document.getElementById('taskAssignee').value = currentTask.assignee ? currentTask.assignee.id : '';

    document.getElementById('taskModalTitle').textContent = 'Edit Task';
    document.getElementById('taskModal').classList.add('active');
}