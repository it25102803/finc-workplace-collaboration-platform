let currentDate = new Date(2026, 0, 1);
let allEvents = [];
let selectedEvent = null;

document.addEventListener('DOMContentLoaded', () => {
    initCalendarListeners();
    fetchCalendarData();
});

function initCalendarListeners() {
    document.getElementById('btnPrevMonth').addEventListener('click', () => {
        currentDate.setMonth(currentDate.getMonth() - 1);
        renderCalendar();
    });

    document.getElementById('btnNextMonth').addEventListener('click', () => {
        currentDate.setMonth(currentDate.getMonth() + 1);
        renderCalendar();
    });

    document.getElementById('btnToday').addEventListener('click', () => {
        currentDate = new Date();
        renderCalendar();
    });

    document.getElementById('eventTypeFilter').addEventListener('change', renderCalendar);

    const modal = document.getElementById('eventModal');
    document.getElementById('btnOpenModal').addEventListener('click', () => {
        document.getElementById('eventForm').reset();
        document.getElementById('eventDate').value = formatDateISO(new Date());
        modal.classList.add('active');
    });

    document.getElementById('btnCloseModal').addEventListener('click', () => modal.classList.remove('active'));
    document.getElementById('btnCancelModal').addEventListener('click', () => modal.classList.remove('active'));
    document.getElementById('eventForm').addEventListener('submit', handleCreateEvent);

    const detailModal = document.getElementById('detailModal');
    document.getElementById('btnCloseDetail').addEventListener('click', () => detailModal.classList.remove('active'));
    document.getElementById('btnCloseDetailBtn').addEventListener('click', () => detailModal.classList.remove('active'));
    document.getElementById('btnDeleteEvent').addEventListener('click', handleDeleteCalendarEvent);
}

async function fetchCalendarData() {
    try {
        const res = await fetch('/api/calendar/events');
        if (res.ok) {
            allEvents = await res.json();
            renderCalendar();
        }
    } catch (e) {
        console.error('Error fetching calendar events:', e);
    }
}

function renderCalendar() {
    const year = currentDate.getFullYear();
    const month = currentDate.getMonth();

    const monthNames = [
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    ];
    document.getElementById('currentMonthLabel').textContent = `${monthNames[month]} ${year}`;

    const grid = document.getElementById('calendarGrid');
    grid.innerHTML = '';

    const firstDayIndex = new Date(year, month, 1).getDay();
    const totalDays = new Date(year, month + 1, 0).getDate();
    const prevMonthTotalDays = new Date(year, month, 0).getDate();

    const filter = document.getElementById('eventTypeFilter').value;
    const now = new Date();

    for (let i = firstDayIndex; i > 0; i--) {
        const dayDiv = document.createElement('div');
        dayDiv.className = 'calendar-day other-month';
        dayDiv.innerHTML = `<div class="day-header"><span class="day-number">${prevMonthTotalDays - i + 1}</span></div>`;
        grid.appendChild(dayDiv);
    }

    for (let day = 1; day <= totalDays; day++) {
        const dayDiv = document.createElement('div');
        dayDiv.className = 'calendar-day';

        const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
        if (now.getFullYear() === year && now.getMonth() === month && now.getDate() === day) {
            dayDiv.classList.add('today');
        }

        dayDiv.innerHTML = `<div class="day-header"><span class="day-number">${day}</span></div>`;

        const dayEvents = allEvents.filter(ev => {
            const evDate = ev.eventDate || (ev.startTime ? ev.startTime.split('T')[0] : '');
            if (evDate !== dateStr) return false;
            if (filter === 'ALL') return true;
            return ev.eventType === filter;
        });

        dayEvents.forEach(ev => {
            const chip = document.createElement('div');
            chip.className = `event-chip ${getChipClass(ev)}`;
            chip.textContent = ev.title;
            chip.title = ev.title;
            chip.addEventListener('click', (e) => {
                e.stopPropagation();
                openEventDetail(ev);
            });
            dayDiv.appendChild(chip);
        });

        dayDiv.addEventListener('click', () => {
            document.getElementById('eventForm').reset();
            document.getElementById('eventDate').value = dateStr;
            document.getElementById('eventModal').classList.add('active');
        });

        grid.appendChild(dayDiv);
    }

    const totalSlots = firstDayIndex + totalDays;
    const remainingSlots = (totalSlots % 7 === 0) ? 0 : 7 - (totalSlots % 7);
    for (let j = 1; j <= remainingSlots; j++) {
        const dayDiv = document.createElement('div');
        dayDiv.className = 'calendar-day other-month';
        dayDiv.innerHTML = `<div class="day-header"><span class="day-number">${j}</span></div>`;
        grid.appendChild(dayDiv);
    }
}

function getChipClass(ev) {
    if (ev.eventType === 'HOLIDAY') {
        return ev.title.includes('Poya') ? 'chip-poya' : 'chip-holiday';
    }
    if (ev.eventType === 'DEADLINE') return 'chip-deadline';
    if (ev.eventType === 'COMPANY_EVENT') return 'chip-company';
    return 'chip-meeting';
}

async function handleCreateEvent(e) {
    e.preventDefault();
    const title = document.getElementById('eventTitle').value;
    const eventType = document.getElementById('eventType').value;
    const date = document.getElementById('eventDate').value;
    const startTime = document.getElementById('eventStartTime').value;
    const endTime = document.getElementById('eventEndTime').value;
    const location = document.getElementById('eventLocation').value;
    const description = document.getElementById('eventDesc').value;

    const payload = {
        title,
        eventType,
        eventDate: date,
        startTime: `${date}T${startTime}:00`,
        endTime: `${date}T${endTime}:00`,
        location,
        description,
        isAllDay: false,
        visibility: 'PUBLIC'
    };

    try {
        const res = await fetch('/api/calendar/events?userId=1', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (res.ok) {
            document.getElementById('eventModal').classList.remove('active');
            fetchCalendarData();
        }
    } catch (err) {
        console.error('Error saving event:', err);
    }
}

function openEventDetail(ev) {
    selectedEvent = ev;
    document.getElementById('detailTitle').textContent = ev.title;
    const badge = document.getElementById('detailBadge');
    badge.textContent = ev.eventType.replace('_', ' ');
    badge.className = `detail-badge ${getChipClass(ev)}`;

    document.getElementById('detailDate').textContent = ev.eventDate || (ev.startTime ? ev.startTime.split('T')[0] : '');
    document.getElementById('detailTime').textContent = (ev.startTime && ev.endTime && !ev.allDay)
        ? `${ev.startTime.split('T')[1].substring(0, 5)} - ${ev.endTime.split('T')[1].substring(0, 5)}`
        : 'All Day';

    const locRow = document.getElementById('detailLocationRow');
    if (ev.location) {
        locRow.style.display = 'flex';
        document.getElementById('detailLocation').textContent = ev.location;
    } else {
        locRow.style.display = 'none';
    }

    document.getElementById('detailDesc').textContent = ev.description || 'No description provided.';
    document.getElementById('btnDeleteEvent').style.display = (ev.eventId && ev.eventType !== 'HOLIDAY') ? 'inline-flex' : 'none';
    document.getElementById('detailModal').classList.add('active');
}

async function handleDeleteCalendarEvent() {
    if (!selectedEvent || !selectedEvent.eventId || !confirm('Delete event?')) return;
    try {
        await fetch(`/api/calendar/events/${selectedEvent.eventId}`, { method: 'DELETE' });
        document.getElementById('detailModal').classList.remove('active');
        fetchCalendarData();
    } catch (e) {
        console.error('Error deleting event:', e);
    }
}

function formatDateISO(date) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
}