let allTasks = [];
let assignableUsers = [];
let currentTask = null;
// The logged-in user (saved by the login page); api.js sends it with every request.
const currentUserId = Number(localStorage.getItem('userId')) || null;
const currentRole = (localStorage.getItem('role') || '').replace('ROLE_', '');
// Team leaders and administrators create, edit and delete tasks; everyone updates their own.
const canManageTasks = ['SYSTEM_ADMIN', 'DEPT_ADMIN', 'TEAM_LEADER'].includes(currentRole);

document.addEventListener('DOMContentLoaded', () => {
    initTaskEventListeners();
    applyRoleToPage();
    fetchUsers();
    fetchTasks();
});

function escapeHtml(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function applyRoleToPage() {
    if (!canManageTasks) {
        ['btnOpenTaskModal', 'btnEditTask', 'btnDeleteTask'].forEach(id => {
            const el = document.getElementById(id);
            if (el) el.style.display = 'none';
        });
    }
}

// Assignee, creator, or a team leader/admin may change status, comment and attach files.
function canWorkOn(task) {
    if (canManageTasks) return true;
    const isAssignee = task.assignee && task.assignee.id === currentUserId;
    const isCreator = task.creator && task.creator.id === currentUserId;
    return Boolean(isAssignee || isCreator);
}

async function fetchUsers() {
    if (!canManageTasks) return;
    try {
        const res = await fetch('/api/tasks/users');
        if (res.ok) {
            assignableUsers = await res.json();
            populateAssigneeDropdown();
        }
    } catch (e) {
        console.warn('Could not load dynamic users:', e);
    }
}

function populateAssigneeDropdown() {
    const select = document.getElementById('taskAssignee');
    if (!select) return;
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

    // Real-Time Search Handler
    const searchInput = document.getElementById('taskSearchInput');
    if (searchInput) {
        searchInput.addEventListener('input', () => {
            renderKanban();
        });
    }

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
    document.getElementById('attachmentForm').addEventListener('submit', handleAddAttachment);
    document.getElementById('viewStatusSelect').addEventListener('change', handleStatusChange);
    document.getElementById('btnDeleteTask').addEventListener('click', handleDeleteTask);
    document.getElementById('btnEditTask').addEventListener('click', handleOpenEditTask);
}

async function fetchTasks() {
    try {
        const res = await fetch('/api/tasks');
        if (res.ok) {
            allTasks = await res.json();
            renderKanban();
        }
    } catch (e) {
        console.error('Error fetching tasks:', e);
    }
}

function renderKanban() {
    const searchQuery = (document.getElementById('taskSearchInput')?.value || '').trim().toLowerCase();

    ['TODO', 'IN_PROGRESS', 'IN_REVIEW', 'COMPLETED'].forEach(status => {
        const container = document.getElementById(`cards-${status}`);
        const countBadge = document.getElementById(`count-${status}`);
        if (!container || !countBadge) return;
        container.innerHTML = '';

        // Filter by Status and Search Query
        const statusTasks = allTasks.filter(task => {
            if (task.status !== status) return false;
            if (!searchQuery) return true;

            const titleMatch = task.title && task.title.toLowerCase().includes(searchQuery);
            const descMatch = task.description && task.description.toLowerCase().includes(searchQuery);
            const priorityMatch = task.priority && task.priority.toLowerCase().includes(searchQuery);

            const assigneeName = task.assignee
                ? `${task.assignee.firstName || ''} ${task.assignee.lastName || ''} ${task.assignee.username || ''}`.toLowerCase()
                : 'all members';
            const assigneeMatch = assigneeName.includes(searchQuery);

            return titleMatch || descMatch || priorityMatch || assigneeMatch;
        });

        countBadge.textContent = statusTasks.length;

        statusTasks.forEach(task => {
            const card = document.createElement('div');
            card.className = 'task-card';

            const assigneeBadge = task.assignee
                ? `<span class="badge badge-user"><i class="fa-solid fa-user"></i> ${escapeHtml(task.assignee.firstName || task.assignee.username)}</span>`
                : `<span class="badge badge-all"><i class="fa-solid fa-users"></i> All Members</span>`;
            const priority = escapeHtml(task.priority || 'MEDIUM');

            card.innerHTML = `
                <div class="card-title">${escapeHtml(task.title)}</div>
                <div class="progress-bar-bg">
                    <div class="progress-bar-fill" style="width: ${Number(task.progress) || 0}%"></div>
                </div>
                <div class="card-meta">
                    <span class="badge badge-${priority.toLowerCase()}">${priority}</span>
                    ${assigneeBadge}
                    <span>${task.dueDate ? '<i class="fa-regular fa-clock"></i> ' + escapeHtml(task.dueDate) : ''}</span>
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

    let url = taskId ? `/api/tasks/${taskId}` : '/api/tasks';
    if (assignedUserId) {
        url += `?assignedUserId=${encodeURIComponent(assignedUserId)}`;
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

    const allowed = canWorkOn(task);
    document.getElementById('statusUpdateBox').style.display = allowed ? '' : 'none';
    document.getElementById('viewStatusSelect').value = task.status;
    document.getElementById('commentForm').style.display = allowed ? '' : 'none';
    document.getElementById('attachmentForm').style.display = allowed ? '' : 'none';

    loadComments(task.taskId);
    loadAttachments(task.taskId);
    if (allowed) loadMyFiles();
    document.getElementById('taskDetailModal').classList.add('active');
}

async function handleStatusChange(e) {
    if (!currentTask) return;
    const status = e.target.value;
    try {
        const res = await fetch(`/api/tasks/${currentTask.taskId}/status?status=${encodeURIComponent(status)}`,{ method: 'PATCH' });
        if (res.ok) {
            currentTask = await res.json();
            document.getElementById('viewStatus').textContent = currentTask.status;
            fetchTasks();
        } else {
            alert('Could not update status: ' + await res.text());
            e.target.value = currentTask.status;
        }
    } catch (err) {
        console.error('Error updating status:', err);
    }
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
                bubble.innerHTML = `<strong>${escapeHtml(authorName)}:</strong> ${escapeHtml(c.content)}`;
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
        const res = await fetch(`/api/tasks/${currentTask.taskId}/comments`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ content })
        });
        if (res.ok) {
            input.value = '';
            loadComments(currentTask.taskId);
        } else {
            alert('Could not add comment: ' + await res.text());
        }
    } catch (e) {
        console.error('Error adding comment:', e);
    }
}

// ---- Attachments (files already uploaded in the File Manager) ----

    async function loadAttachments(taskId) {
        const list = document.getElementById('attachmentList');
        list.innerHTML = '';
        try {
            const res = await fetch(`/api/tasks/${taskId}/attachments`);
            if (!res.ok) return;
            const attachments = await res.json();
            if (!attachments.length) {
                list.innerHTML = '<div class="comment-bubble">No files attached yet.</div>';
                return;
            }
            attachments.forEach(a => {
                const row = document.createElement('div');
                row.className = 'comment-bubble';
                const link = a.fileId
                    ? `<a href="${escapeHtml(window.withAuth(`/api/files/${a.fileId}/download`))}" target="_blank" rel="noopener">${escapeHtml(a.fileName)}</a>`
                    : escapeHtml(a.fileName);
                const remove = canWorkOn(currentTask)
                    ? ` <button type="button" class="btn btn-outline" data-remove="${a.attachmentId}" title="Remove">&times;</button>`
                    : '';
                row.innerHTML = `<i class="fa-solid fa-paperclip"></i> ${link}${remove}`;
                list.appendChild(row);
            });
            list.querySelectorAll('[data-remove]').forEach(btn => {
                btn.addEventListener('click', () => handleRemoveAttachment(btn.dataset.remove));
            });
        } catch (e) {
            console.error('Error loading attachments:', e);
        }
    }

    async function loadMyFiles() {
        const select = document.getElementById('attachmentFileSelect');
        select.innerHTML = '<option value="">Choose one of your files from the File Manager...</option>';
        if (!currentUserId) return;
        try {
            const res = await fetch(`/api/files/user/${currentUserId}`);
            if (!res.ok) return;
            const files = await res.json();
            files.forEach(f => {
                const opt = document.createElement('option');
                opt.value = f.fileId ?? f.id;
                opt.textContent = f.fileName ?? f.name;
                select.appendChild(opt);
            });
        } catch (e) {
            console.error('Error loading files:', e);
        }
    }

    async function handleAddAttachment(e) {
        e.preventDefault();
        if (!currentTask) return;
        const fileId = document.getElementById('attachmentFileSelect').value;
        if (!fileId) return;
        const res = await fetch(`/api/tasks/${currentTask.taskId}/attachments?fileId=${encodeURIComponent(fileId)}`, { method: 'POST' });
        if (res.ok) {
            loadAttachments(currentTask.taskId);
        } else {
            alert('Could not attach file: ' + await res.text());
        }
    }

    async function handleRemoveAttachment(attachmentId) {
        if (!currentTask || !confirm('Remove this attachment from the task?')) return;
        const res = await fetch(`/api/tasks/${currentTask.taskId}/attachments/${attachmentId}`, { method: 'DELETE' });
        if (res.ok) {
            loadAttachments(currentTask.taskId);
        }
    }

    async function handleDeleteTask() {
        if (!currentTask || !confirm('Are you sure you want to delete this task?')) return;
        try {
            const res = await fetch(`/api/tasks/${currentTask.taskId}`, { method: 'DELETE' });
            if (!res.ok) {
                alert('Could not delete task: ' + await res.text());
                return;
            }
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
