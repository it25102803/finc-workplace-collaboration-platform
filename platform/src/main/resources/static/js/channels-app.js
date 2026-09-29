const API_URL = "/api/users";
const API_USERS = API_URL;
const API_CHANNELS = "/api/channels";
const API_KNOWLEDGE = "/api/knowledge";
const API_NOTIFICATIONS = "/api/notifications";
const CURRENT_USER = {
    name: "Admin User",
    email: "admin@fincacademy.com"
};

const state = {
    users: [],
    channels: [],
    knowledge: [],
    selectedChannelId: null,
    notifications: [],
    renderedMessages: []
};

let users = [];
let messagePollingTimer;
let stompClient;
let channelSubscription;


// ==========================================
// LOAD USERS
// ==========================================

async function loadUsers() {

    try {

        const response = await fetch(API_URL);

        if (!response.ok) {
            throw new Error("Failed to load users");
        }

        users = await response.json();

        updateDashboard();

        displayUsers(users);

        displayRecentUsers();

    } catch (error) {

        console.error(error);

        showToast(
            "Could not connect to Spring Boot backend"
        );
    }
}


// ==========================================
// DASHBOARD
// ==========================================

function updateDashboard() {

    const totalUsers = document.getElementById("totalUsers");
    const onlineUsers = document.getElementById("onlineUsers");
    const activeUsers = document.getElementById("activeUsers");

    if (!totalUsers || !onlineUsers || !activeUsers) {
        return;
    }

    totalUsers.textContent = users.length;
    onlineUsers.textContent = users.filter(user => user.online).length;
    activeUsers.textContent = users.filter(user => user.active).length;
}


// ==========================================
// DISPLAY USERS
// ==========================================

function displayUsers(list) {

    const table = document.getElementById("userTable");

    if (!table) {
        return;
    }

    table.innerHTML = "";

    if (list.length === 0) {

        table.innerHTML = `
            <tr>
                <td colspan="6"
                    style="text-align:center;padding:40px">
                    No users found
                </td>
            </tr>
        `;

        return;
    }


    list.forEach(user => {

        const initials =
            user.name
                .split(" ")
                .map(word => word[0])
                .join("")
                .substring(0, 2)
                .toUpperCase();


        table.innerHTML += `

            <tr>

                <td>

                    <div class="user-info">

                        <div class="user-avatar">
                            ${initials}
                        </div>

                        <div>

                            <strong>
                                ${user.name}
                            </strong>

                            <div class="user-email">
                                ${user.email}
                            </div>

                        </div>

                    </div>

                </td>


                <td>
                    ${user.department}
                </td>


                <td>
                    ${formatRole(user.role)}
                </td>


                <td>

                    <span class="status
                        ${user.active
                            ? "active"
                            : "disabled"}">

                        ${user.active
                            ? "Active"
                            : "Disabled"}

                    </span>

                </td>


                <td>

                    <span class="${user.online
                        ? "online"
                        : "offline"}">

                        ●
                        ${user.online
                            ? "Online"
                            : "Offline"}

                    </span>

                </td>


                <td>

                    <button
                        class="action-button"
                        onclick="editUser(${user.id})"
                        title="Edit">

                        ✏️

                    </button>


                    ${
                        user.active
                        ?
                        `
                        <button
                            class="action-button"
                            onclick="disableUser(${user.id})"
                            title="Disable">

                            🚫

                        </button>
                        `
                        :
                        ""
                    }

                </td>

            </tr>
        `;
    });
}


// ==========================================
// RECENT USERS
// ==========================================

function displayRecentUsers() {

    const container = document.getElementById("recentUsers");

    if (!container) {
        return;
    }

    container.innerHTML = "";

    users.slice(-5).reverse().forEach(user => {

        const initials =
            user.name
                .split(" ")
                .map(x => x[0])
                .join("")
                .substring(0, 2)
                .toUpperCase();


        container.innerHTML += `

            <div class="user-info"
                 style="margin-bottom:15px">

                <div class="user-avatar">
                    ${initials}
                </div>

                <div>

                    <strong>
                        ${user.name}
                    </strong>

                    <div class="user-email">
                        ${user.department}
                        •
                        ${formatRole(user.role)}
                    </div>

                </div>

            </div>

        `;
    });
}


// ==========================================
// FORMAT ROLE
// ==========================================

function formatRole(role) {

    return role
        .toLowerCase()
        .replaceAll("_", " ")
        .replace(/\b\w/g,
            letter => letter.toUpperCase());
}


// ==========================================
// CREATE USER
// ==========================================

function openCreateUser() {

    document.getElementById("modalTitle")
        .textContent = "Add User";

    document.getElementById("userForm")
        .reset();

    document.getElementById("userId")
        .value = "";

    document.getElementById("userModal")
        .classList.add("show");
}


// ==========================================
// EDIT USER
// ==========================================

function editUser(id) {

    const user =
        users.find(u => u.id === id);

    if (!user) return;


    document.getElementById("modalTitle")
        .textContent = "Edit User";

    document.getElementById("userId")
        .value = user.id;

    document.getElementById("name")
        .value = user.name;

    document.getElementById("email")
        .value = user.email;

    document.getElementById("password")
        .value = "";

    document.getElementById("department")
        .value = user.department;

    document.getElementById("role")
        .value = user.role;


    document.getElementById("userModal")
        .classList.add("show");
}


// ==========================================
// SAVE USER
// ==========================================

const userForm = document.getElementById("userForm");

if (userForm) {
    userForm.addEventListener("submit", async function(event) {

        event.preventDefault();

        const id = document.getElementById("userId")?.value ?? "";

        const user = {
            name: document.getElementById("name")?.value ?? "",
            email: document.getElementById("email")?.value ?? "",
            password: document.getElementById("password")?.value ?? "",
            department: document.getElementById("department")?.value ?? "",
            role: document.getElementById("role")?.value ?? ""
        };

        try {
            let response;

            if (id) {
                response = await fetch(
                    `${API_URL}/${id}`,
                    {
                        method: "PUT",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify(user)
                    }
                );
            } else {
                response = await fetch(
                    API_URL,
                    {
                        method: "POST",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify(user)
                    }
                );
            }

            if (!response.ok) {
                throw new Error("Save failed");
            }

            closeModal();
            showToast(id ? "User updated successfully" : "User created successfully");
            await loadUsers();

        } catch (error) {
            console.error(error);
            showToast("Could not save user");
        }
    });
}


// ==========================================
// DISABLE USER
// ==========================================

async function disableUser(id) {

    const user =
        users.find(u => u.id === id);


    if (!user) return;


    const confirmDisable =
        confirm(
            `Disable ${user.name}?`
        );


    if (!confirmDisable) {
        return;
    }


    try {

        const response =
            await fetch(
                `${API_URL}/${id}/disable`,
                {
                    method: "PUT"
                }
            );


        if (!response.ok) {
            throw new Error("Disable failed");
        }


        showToast(
            "User disabled successfully"
        );


        await loadUsers();


    } catch (error) {

        console.error(error);

        showToast(
            "Could not disable user"
        );
    }
}


// ==========================================
// SEARCH / FILTER
// ==========================================

function filterUsers() {

    const search =
        document.getElementById("userSearch")
            .value
            .toLowerCase();


    const department =
        document.getElementById(
            "departmentFilter"
        ).value;


    const filtered =
        users.filter(user => {

            const matchesSearch =
                user.name
                    .toLowerCase()
                    .includes(search)
                ||
                user.email
                    .toLowerCase()
                    .includes(search);


            const matchesDepartment =
                !department ||
                user.department === department;


            return matchesSearch &&
                   matchesDepartment;
        });


    displayUsers(filtered);
}


// ==========================================
// GLOBAL SEARCH
// ==========================================

function globalSearch() {

    const keyword =
        document.getElementById("globalSearch")
            .value
            .toLowerCase();


    if (!keyword) {
        return;
    }


    const results =
        users.filter(user =>
            user.name
                .toLowerCase()
                .includes(keyword)
            ||
            user.email
                .toLowerCase()
                .includes(keyword)
        );


    showPage(
        "users",
        document.querySelectorAll(".nav-item")[1]
    );


    document.getElementById("userSearch")
        .value = keyword;


    displayUsers(results);
}


// ==========================================
// NAVIGATION
// ==========================================

function showPage(pageId, button) {

    document
        .querySelectorAll(".page")
        .forEach(page => {

            page.classList.remove(
                "active-page"
            );

        });


    document
        .getElementById(pageId)
        .classList.add(
            "active-page"
        );


    document
        .querySelectorAll(".nav-item")
        .forEach(item => {

            item.classList.remove("active");

        });


    if (button) {
        button.classList.add("active");
    }


    const titles = {

        dashboard: [
            "Dashboard",
            "Welcome to your workplace"
        ],

        users: [
            "User Management",
            "Manage employees, roles and departments"
        ],

        messages: [
            "Messages",
            "Communicate with your team"
        ],

        tasks: [
            "Task Management",
            "Manage workplace tasks"
        ],

        calendar: [
            "Calendar",
            "Meetings and company events"
        ],

        files: [
            "File Management",
            "Manage workplace documents"
        ],

        knowledge: [
            "Knowledge Base",
            "Company knowledge repository"
        ]
    };


    document.getElementById("pageTitle")
        .textContent =
        titles[pageId][0];


    document.getElementById("pageDescription")
        .textContent =
        titles[pageId][1];
}


function openUsers() {

    const userButton =
        document.querySelectorAll(".nav-item")[1];

    showPage("users", userButton);
}


// ==========================================
// MODAL
// ==========================================

function closeModal() {

    const userModal = document.getElementById("userModal");

    if (!userModal) {
        return;
    }

    userModal.classList.remove("show");
}


// ==========================================
// TOAST
// ==========================================

function showToast(message) {

    const toast = document.getElementById("toast");

    if (!toast) {
        return;
    }

    toast.textContent = message;
    toast.classList.add("show");

    setTimeout(() => {
        toast.classList.remove("show");
    }, 3000);
}


const specialDays = [
    {
        date: "2026-01-03",
        title: "Duruthu Full Moon Poya Day",
        type: "poya"
    },
    {
        date: "2026-02-01",
        title: "Navam Full Moon Poya Day",
        type: "poya"
    },
    {
        date: "2026-03-03",
        title: "Madin Full Moon Poya Day",
        type: "poya"
    },
    {
        date: "2026-04-01",
        title: "Bak Full Moon Poya Day",
        type: "poya"
    },
    {
        date: "2026-05-01",
        title: "Vesak Full Moon Poya Day",
        type: "poya"
    },

    // Example public holidays
    {
        date: "2026-05-01",
        title: "May Day",
        type: "holiday"
    },

    {
        date: "2026-02-04",
        title: "Independence Day",
        type: "holiday"
    }
];
// ==========================================
// START APPLICATION
// ==========================================

loadUsers();
let currentDate = new Date();

// ==============================
// SPECIAL DAYS
// ==============================



function renderCalendar(){

    

    const grid = document.getElementById("calendarGrid");
    const title = document.getElementById("monthYear");

    if(!grid || !title) return;

    grid.innerHTML = "";

    const month = currentDate.getMonth();
    const year = currentDate.getFullYear();

    title.textContent = currentDate.toLocaleString(
        "default",
        {
            month: "long",
            year: "numeric"
        }
    );


    // ==========================
    // DAY NAMES
    // ==========================

    const days = [
        "Sun",
        "Mon",
        "Tue",
        "Wed",
        "Thu",
        "Fri",
        "Sat"
    ];

    days.forEach(d => {

        grid.innerHTML += `
            <div class="day-name">
                ${d}
            </div>
        `;

    });


    // ==========================
    // FIRST DAY OF MONTH
    // ==========================

    const firstDay =
        new Date(year, month, 1).getDay();


    // ==========================
    // TOTAL DAYS
    // ==========================

    const totalDays =
        new Date(
            year,
            month + 1,
            0
        ).getDate();


    // Empty spaces before first day

    for(let i = 0; i < firstDay; i++){

        grid.innerHTML += `
            <div class="day empty-day"></div>
        `;

    }


    // ==========================
    // TODAY
    // ==========================

    const today = new Date();


    // ==========================
    // CREATE DAYS
    // ==========================

    for(let d = 1; d <= totalDays; d++){

        const isToday =
            d === today.getDate() &&
            month === today.getMonth() &&
            year === today.getFullYear();


        // Date format YYYY-MM-DD

        const monthNumber =
            String(month + 1).padStart(2, "0");

        const dayNumber =
            String(d).padStart(2, "0");

        const dateString =
            `${year}-${monthNumber}-${dayNumber}`;


        // ==========================
        // FIND SPECIAL DAYS
        // ==========================

        const specialDayList =
            specialDays.filter(
                special =>
                    special.date === dateString
            );


        // ==========================
        // SPECIAL DAY HTML
        // ==========================

        let specialHTML = "";


        specialDayList.forEach(special => {

            let icon = "⭐";

            if(special.type === "poya"){
                icon = "🟣";
            }

            if(special.type === "holiday"){
                icon = "🟢";
            }


            specialHTML += `
                <div class="special-day ${special.type}">
                    ${icon} ${special.title}
                </div>
            `;

        });


        // ==========================
        // CREATE CALENDAR DAY
        // ==========================

        grid.innerHTML += `

            <div
                class="day ${isToday ? "today" : ""}"
                data-date="${dateString}"
            >

                <div class="date-number">
                    ${d}
                </div>

                ${specialHTML}

                <button
                    class="add-day-schedule"
                    onclick="openScheduleModal('${dateString}')"
                >
                    + Add Schedule
                </button>

            </div>

        `;

    }

}


function changeMonth(step){
    currentDate.setMonth(currentDate.getMonth()+step);
    renderCalendar();
}
loadUsers();
renderCalendar();
document.addEventListener("DOMContentLoaded", () => {
    bindEvents();
    loadUsers();
    loadChannels();
    loadKnowledge();
    loadNotifications();
    connectLiveUpdates();
    messagePollingTimer = setInterval(() => {
        if (state.selectedChannelId && !stompClient?.connected) {
            loadMessages(state.selectedChannelId).catch(error => console.error(error));
        }
    }, 5000);
});

function bindEvents() {
    document.getElementById("messageForm").addEventListener("submit", sendMessage);
    document.getElementById("notificationBtn").addEventListener("click", toggleNotifications);
    document.getElementById("sidebarToggle").addEventListener("click", toggleSidebar);
    document.getElementById("sidebarBackdrop").addEventListener("click", closeSidebar);
    
    const notificationPanel = document.getElementById("notificationPanel");
    
    // Prevent clicks inside the notification panel from closing it
    if (notificationPanel) {
        notificationPanel.addEventListener("click", event => {
            event.stopPropagation();
        });
    }

    // Updated click listener for both sidebar and notification panel closing
    document.addEventListener("click", event => {
        const sidebar = document.querySelector(".sidebar");
        const toggle = document.getElementById("sidebarToggle");
        if (sidebar && !sidebar.contains(event.target) && event.target !== toggle && !toggle.contains(event.target)) {
            closeSidebar();
        }

        // Close notification panel if it's open and you click outside of it
        if (notificationPanel && !notificationPanel.classList.contains("hidden")) {
            const notificationBtn = document.getElementById("notificationBtn");
            if (event.target !== notificationBtn && !notificationBtn.contains(event.target)) {
                notificationPanel.classList.add("hidden");
            }
        }
    });

    document.getElementById("refreshBtn").addEventListener("click", async () => {
        await Promise.all([loadUsers(), loadChannels(), loadKnowledge()]);
    });
    document.getElementById("createChannelBtn").addEventListener("click", openChannelModal);
    document.getElementById("closeChannelModalBtn").addEventListener("click", closeChannelModal);
    document.getElementById("cancelChannelModalBtn").addEventListener("click", closeChannelModal);
    document.getElementById("channelForm").addEventListener("submit", createChannelFromForm);
    document.getElementById("addMemberBtn").addEventListener("click", addMemberToSelectedChannel);
}
function connectLiveUpdates() {
    if (!window.StompJs) {
        return;
    }

    stompClient = new StompJs.Client({
        brokerURL: `${location.protocol === "https:" ? "wss" : "ws"}://${location.host}/ws`,
        reconnectDelay: 5000
    });

    stompClient.onConnect = () => {
        subscribeToChannel();
        stompClient.subscribe(`/topic/notifications/${CURRENT_USER.email}`, notificationFrame => {
            const notification = JSON.parse(notificationFrame.body);
            state.notifications.unshift(notification);
            renderNotifications();
        });
    };

    stompClient.activate();
}

function subscribeToChannel() {
    if (!stompClient?.connected || !state.selectedChannelId) {
        return;
    }

    channelSubscription?.unsubscribe();
    channelSubscription = stompClient.subscribe(`/topic/channels/${state.selectedChannelId}`, frame => {
        const message = JSON.parse(frame.body);
        if (!state.renderedMessages.some(existing => existing.id === message.id)) {
            renderMessages([...state.renderedMessages, message]);
        }
    });
}

function toggleSidebar() {
    document.body.classList.toggle("sidebar-collapsed");
}

function closeSidebar() {
    document.body.classList.add("sidebar-collapsed");
}

async function loadUsers() {
    const response = await fetch(API_USERS);
    if (!response.ok) {
        throw new Error("Unable to load users");
    }

    state.users = await response.json();
    renderMemberSelector();
    renderDirectMessageList();
}

async function loadChannels() {
    const response = await fetch(API_CHANNELS);
    if (!response.ok) {
        throw new Error("Unable to load channels");
    }

    state.channels = await response.json();
    renderChannelList();
    renderDirectMessageList();

    if (!state.selectedChannelId && state.channels.length > 0) {
        selectChannel(state.channels[0].id);
    } else if (state.selectedChannelId) {
        const currentChannel = state.channels.find(channel => channel.id === state.selectedChannelId);
        if (currentChannel) {
            selectChannel(currentChannel.id);
        } else if (state.channels.length > 0) {
            selectChannel(state.channels[0].id);
        }
    }
}

async function loadKnowledge() {
    const response = await fetch(API_KNOWLEDGE);
    if (!response.ok) {
        throw new Error("Unable to load knowledge base");
    }

    state.knowledge = await response.json();
    renderKnowledgeList();
}

async function loadNotifications() {
    const response = await fetch(`${API_NOTIFICATIONS}?email=${encodeURIComponent(CURRENT_USER.email)}`);
    if (!response.ok) {
        return;
    }

    state.notifications = await response.json();
    renderNotifications();
}

function toggleNotifications(event) {
    if (event) {
        event.stopPropagation(); // Prevents the click from triggering the document click listener immediately
    }
    document.getElementById("notificationPanel").classList.toggle("hidden");
}

function renderNotifications() {
    const panel = document.getElementById("notificationPanel");
    const unread = state.notifications.filter(notification => !notification.read).length;
    const count = document.getElementById("notificationCount");
    count.textContent = unread;
    count.classList.toggle("hidden", unread === 0);

    panel.innerHTML = state.notifications.length
        ? state.notifications.slice(0, 8).map(notification => `
            <button class="notification-item ${notification.read ? "read" : "unread"}" data-id="${notification.id}">
                <strong>${escapeHtml(notification.type.replace("_", " "))}</strong>
                <span>${escapeHtml(notification.message)}</span>
            </button>
        `).join("")
        : '<div class="empty-state">No notifications</div>';

    panel.querySelectorAll(".notification-item").forEach(item => {
        item.addEventListener("click", () => markNotificationRead(Number(item.dataset.id)));
    });
}

async function markNotificationRead(id) {
    await fetch(`${API_NOTIFICATIONS}/${id}/read`, { method: "PUT" });
    const notification = state.notifications.find(item => item.id === id);
    if (notification) {
        notification.read = true;
    }
    renderNotifications();
}

async function loadMessages(channelId) {
    const response = await fetch(`${API_CHANNELS}/${channelId}/messages`);
    if (!response.ok) {
        throw new Error("Unable to load messages");
    }

    const messages = await response.json();
    renderMessages(messages);
}

function renderChannelList() {
    const container = document.getElementById("channelList");

    if (!state.channels.length) {
        container.innerHTML = '<div class="empty-state">No channels yet</div>';
        return;
    }

    container.innerHTML = state.channels
        .map(channel => {
            const isActive = channel.id === state.selectedChannelId ? "active" : "";
            const wrapper = channel.type === "DIRECT" ? "@" : "#";
            const department = channel.department ? ` (${channel.department})` : "";
            return `
                <button class="channel-item ${isActive}" data-id="${channel.id}">
                    <span class="channel-hash">${wrapper}</span>
                    <span>${escapeHtml(channel.name)}${escapeHtml(department)}</span>
                </button>
            `;
        })
        .join("");

    container.querySelectorAll(".channel-item").forEach(button => {
        button.addEventListener("click", () => selectChannel(Number(button.dataset.id)));
    });
}

function renderDirectMessageList() {
    const container = document.getElementById("directMessageList");
    const users = state.users.filter(user => user.email !== CURRENT_USER.email);

    if (!users.length) {
        container.innerHTML = '<div class="empty-state">No teammates</div>';
        return;
    }

    container.innerHTML = users
        .map(user => `
            <button class="channel-item" data-user-email="${user.email}" data-user-name="${user.name}">
                <span class="channel-hash">${user.name.charAt(0).toUpperCase()}</span>
                <span>${escapeHtml(user.name)}</span>
            </button>
        `)
        .join("");

    container.querySelectorAll(".channel-item").forEach(button => {
        button.addEventListener("click", () => {
            startDirectMessage(button.dataset.userEmail, button.dataset.userName);
        });
    });
}
async function deleteMessage(messageId) {
    if (!confirm("Are you sure you want to delete this message?")) {
        return;
    }

    try {
        // This matches your backend controller @DeleteMapping("/messages/{id}") and sends the sender
        const response = await fetch(`/api/messages/${messageId}?sender=${encodeURIComponent(CURRENT_USER.name)}`, {
            method: "DELETE"
        });

        if (response.status === 403) {
            showToast("You can only delete your own messages");
            return;
        }

        if (!response.ok) {
            throw new Error("Unable to delete message");
        }

        showToast("Message deleted successfully");
        await loadMessages(state.selectedChannelId);
    } catch (error) {
        console.error(error);
        showToast("Could not delete message");
    }
}
function renderMessages(messages) {
    const container = document.getElementById("messageList");
    state.renderedMessages = messages;

    if (!messages.length) {
        container.innerHTML = '<div class="empty-state">No messages in this channel yet.</div>';
        return;
    }

    container.innerHTML = messages
        .map(message => {
            const isMine = message.sender === CURRENT_USER.name;
            return `
                <div class="message-bubble ${isMine ? "mine" : ""}">
                    <div class="message-meta">
                        <div>
                            <strong>${escapeHtml(message.sender)}</strong>
                            <span>${formatDate(message.timestamp)}</span>
                        </div>
                        ${isMine ? `<button class="delete-message-btn" onclick="deleteMessage(${message.id})" title="Delete message">🗑️</button>` : ""}
                    </div>
                    ${message.content ? `<p>${escapeHtml(message.content)}</p>` : ""}
                    ${renderAttachment(message)}
                </div>
            `;
        })
        .join("");

    container.scrollTop = container.scrollHeight;
}

function getRenderedMessages() {
    return state.renderedMessages || [];
}

function renderAttachment(message) {
    if (!message.attachmentData) {
        return "";
    }

    const attachmentName = escapeHtml(message.attachmentName || "Attachment");
    const attachmentUrl = `data:${message.attachmentType || "application/octet-stream"};base64,${message.attachmentData}`;

    if ((message.attachmentType || "").startsWith("image/")) {
        return `
            <a href="${attachmentUrl}" download="${attachmentName}" class="attachment-link">
                <img src="${attachmentUrl}" alt="${attachmentName}" class="message-image">
            </a>
        `;
    }

    return `<a href="${attachmentUrl}" download="${attachmentName}" class="attachment-link">${attachmentName}</a>`;
}

function renderMemberSelector() {
    const select = document.getElementById("memberSelector");
    if (!state.users.length) {
        select.innerHTML = '<option value="">No users</option>';
        return;
    }

    const currentChannel = getSelectedChannel();
    const selectedEmails = currentChannel?.memberEmails ?? [];
    const availableUsers = state.users.filter(user => !selectedEmails.includes(user.email));

    if (!availableUsers.length) {
        select.innerHTML = '<option value="">All members added</option>';
        return;
    }

    select.innerHTML = availableUsers
        .map(user => `<option value="${user.email}">${user.name} (${user.email})</option>`)
        .join("");
}

function renderMemberList() {
    const memberList = document.getElementById("memberList");
    const currentChannel = getSelectedChannel();

    if (!currentChannel || !currentChannel.memberEmails?.length) {
        memberList.innerHTML = '<li>No team members yet.</li>';
        return;
    }

    memberList.innerHTML = currentChannel.memberEmails
        .map(email => {
            const user = getUserByEmail(email);
            const label = user ? `${user.name} (${email})` : email;
            return `
                <li>
                    <strong>${escapeHtml(label)}</strong>
                    <span>
                        ${currentChannel.type}
                        <button type="button" class="remove-member-button" data-email="${escapeHtml(email)}" title="Remove member">Remove</button>
                    </span>
                </li>
            `;
        })
        .join("");

    memberList.querySelectorAll(".remove-member-button").forEach(button => {
        button.addEventListener("click", () => removeMemberFromSelectedChannel(button.dataset.email));
    });
}

function renderKnowledgeList() {
    const container = document.getElementById("knowledgeList");

    if (!state.knowledge.length) {
        container.innerHTML = '<li>No knowledge articles yet.</li>';
        return;
    }

    container.innerHTML = state.knowledge
        .slice(0, 5)
        .map(item => `
            <li>
                <strong>${escapeHtml(item.title)}</strong>
                <span>${escapeHtml(item.category)} • ${escapeHtml(item.createdBy)}</span>
            </li>
        `)
        .join("");
}

function getSelectedChannel() {
    return state.channels.find(channel => channel.id === state.selectedChannelId) || null;
}

function getUserByEmail(email) {
    return state.users.find(user => user.email === email) || null;
}

function selectChannel(channelId) {
    state.selectedChannelId = channelId;
    const channel = getSelectedChannel();

    if (!channel) {
        return;
    }

    document.getElementById("chatTitle").textContent = channel.name;
    document.getElementById("channelMeta").textContent = channel.department
        ? `${channel.department} department | ${channel.description || channel.type + " channel"}`
        : (channel.description || `${channel.type} channel`);

    renderChannelList();
    renderMemberSelector();
    renderMemberList();
    loadMessages(channelId);
    subscribeToChannel();
}

async function sendMessage(event) {
    event.preventDefault();

    const input = document.getElementById("messageInput");
    const attachmentInput = document.getElementById("attachmentInput");
    const content = input.value.trim();
    const attachment = attachmentInput.files[0];

    if ((!content && !attachment) || !state.selectedChannelId) {
        return;
    }

    let response;
    if (attachment) {
        const formData = new FormData();
        formData.append("sender", CURRENT_USER.name);
        formData.append("content", content);
        formData.append("attachment", attachment);
        response = await fetch(`${API_CHANNELS}/${state.selectedChannelId}/messages/attachment`, {
            method: "POST",
            body: formData
        });
    } else {
        const payload = {
            sender: CURRENT_USER.name,
            content,
            channel: {
                id: state.selectedChannelId
            }
        };

        response = await fetch(`${API_CHANNELS}/${state.selectedChannelId}/messages`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        });
    }

    if (!response.ok) {
        throw new Error("Unable to send message");
    }

    input.value = "";
    attachmentInput.value = "";
    await loadMessages(state.selectedChannelId);
}

async function startDirectMessage(userEmail, userName) {
    const directChannel = state.channels.find(channel => {
        const emails = channel.memberEmails || [];
        return channel.type === "DIRECT"
            && emails.includes(CURRENT_USER.email)
            && emails.includes(userEmail);
    });

    if (directChannel) {
        selectChannel(directChannel.id);
        return;
    }

    const payload = {
        name: userName,
        description: `Direct message with ${userName}`,
        type: "DIRECT",
        memberEmails: [CURRENT_USER.email, userEmail]
    };

    const response = await fetch(API_CHANNELS, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
    });

    if (!response.ok) {
        throw new Error("Unable to create direct message channel");
    }

    await loadChannels();
}

function openChannelModal() {
    document.getElementById("channelModal").classList.remove("hidden");
}

function closeChannelModal() {
    document.getElementById("channelModal").classList.add("hidden");
    document.getElementById("channelForm").reset();
}

async function createChannelFromForm(event) {
    event.preventDefault();

    const name = document.getElementById("channelName").value.trim();
    const description = document.getElementById("channelDescription").value.trim();
    const department = document.getElementById("channelDepartment").value.trim();
    const type = document.getElementById("channelType").value;

    if (!name) {
        return;
    }

    const payload = {
        name,
        description,
        department: department || null,
        type,
        memberEmails: [CURRENT_USER.email]
    };

    const response = await fetch(API_CHANNELS, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
    });

    if (!response.ok) {
        throw new Error("Unable to create channel");
    }

    closeChannelModal();
    await loadChannels();
}

async function addMemberToSelectedChannel() {
    const currentChannel = getSelectedChannel();
    if (!currentChannel) {
        return;
    }

    const email = document.getElementById("memberSelector").value;
    if (!email) {
        return;
    }

    const response = await fetch(`${API_CHANNELS}/${currentChannel.id}/members`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ email })
    });

    if (!response.ok) {
        throw new Error("Unable to add member");
    }

    await loadChannels();
}

async function removeMemberFromSelectedChannel(email) {
    const currentChannel = getSelectedChannel();
    if (!currentChannel || !email) {
        return;
    }

    const response = await fetch(`${API_CHANNELS}/${currentChannel.id}/members?email=${encodeURIComponent(email)}`, {
        method: "DELETE"
    });

    if (!response.ok) {
        throw new Error("Unable to remove member");
    }

    await loadChannels();
}

function formatDate(value) {
    if (!value) {
        return "Now";
    }

    const date = new Date(value);
    return date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
}

function escapeHtml(text) {
    return String(text)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/\"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

