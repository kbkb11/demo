
const state = {
  baseUrl: 'http://localhost:8080',
  auth: { username: 'admin', password: 'admin123' },
  config: null,
  charts: {},
  students: [],
  courses: [],
  scores: []
};

let elements;
let statusTimer;

document.addEventListener('DOMContentLoaded', () => {
  elements = {
    statusIndicator: document.getElementById('statusIndicator'),
    statusMessage: document.getElementById('statusMessage'),
    credentialSummary: document.getElementById('credentialSummary'),
    apiBaseUrl: document.getElementById('apiBaseUrl'),
    dashboard: document.getElementById('dashboard'),
    tables: document.getElementById('tables'),
    lists: document.getElementById('lists'),
    loginForm: document.getElementById('loginForm'),
    usernameInput: document.getElementById('usernameInput'),
    passwordInput: document.getElementById('passwordInput'),
    refreshButton: document.getElementById('refreshButton'),
    studentForm: document.getElementById('studentForm'),
    courseForm: document.getElementById('courseForm'),
    scoreForm: document.getElementById('scoreForm'),
    studentFormReset: document.getElementById('studentFormReset'),
    courseFormReset: document.getElementById('courseFormReset'),
    scoreFormReset: document.getElementById('scoreFormReset'),
    studentsTableBody: document.getElementById('studentsTableBody'),
    coursesTableBody: document.getElementById('coursesTableBody'),
    scoresTableBody: document.getElementById('scoresTableBody'),
    scoreStudentSelect: document.getElementById('scoreStudentSelect'),
    scoreCourseSelect: document.getElementById('scoreCourseSelect'),
    scoreIdInput: document.getElementById('scoreIdInput'),
    scoreValueInput: document.getElementById('scoreValueInput'),
    studentIdInput: document.getElementById('studentIdInput'),
    courseIdInput: document.getElementById('courseIdInput'),
    studentNumberInput: document.getElementById('studentNumberInput'),
    studentNameInput: document.getElementById('studentNameInput'),
    studentClazzInput: document.getElementById('studentClazzInput'),
    studentMajorInput: document.getElementById('studentMajorInput'),
    courseNameInput: document.getElementById('courseNameInput'),
    courseCreditInput: document.getElementById('courseCreditInput'),
    courseTeacherInput: document.getElementById('courseTeacherInput'),
    studentExcelForm: document.getElementById('studentExcelForm'),
    scoreExcelForm: document.getElementById('scoreExcelForm'),
    studentExcelFile: document.getElementById('studentExcelFile'),
    scoreExcelFile: document.getElementById('scoreExcelFile'),
    excelStatus: document.getElementById('excelStatus')
  };

  bindUI();
  init();
});

function bindUI() {
  elements.loginForm?.addEventListener('submit', handleCredentialSubmit);
  elements.refreshButton?.addEventListener('click', () => loadConfig());
  elements.studentForm?.addEventListener('submit', handleStudentSubmit);
  elements.courseForm?.addEventListener('submit', handleCourseSubmit);
  elements.scoreForm?.addEventListener('submit', handleScoreSubmit);
  elements.studentFormReset?.addEventListener('click', clearStudentForm);
  elements.courseFormReset?.addEventListener('click', clearCourseForm);
  elements.scoreFormReset?.addEventListener('click', clearScoreForm);
  elements.studentExcelForm?.addEventListener('submit', event => {
    handleExcelImport(event, '/api/excel/import/students', '学生导入');
  });
  elements.scoreExcelForm?.addEventListener('submit', event => {
    handleExcelImport(event, '/api/excel/import/scores', '成绩导入');
  });
  document.querySelectorAll('.excel-actions button[data-endpoint]').forEach(button => {
    button.addEventListener('click', () => handleExport(button));
  });
}

async function init() {
  updateCredentialSummary();
  setStatusIndicator('pending', '待登录');
  try {
    await loadConfig();
  } catch (error) {
    showStatusMessage('配置加载失败：' + error.message, 'error');
    setStatusIndicator('error', '配置读取出错');
  }
}

async function loadConfig() {
  setStatusIndicator('pending', '加载配置...');
  const response = await fetch('/config/dashboard-config.json');
  if (!response.ok) {
    throw new Error('无法读取配置：' + response.status + ' ' + response.statusText);
  }
  const config = await response.json();
  state.config = config;
  const configuredBase = (config.baseUrl || '').trim();
  if (configuredBase) {
    state.baseUrl = configuredBase;
  }
  if (elements.apiBaseUrl) {
    elements.apiBaseUrl.textContent = state.baseUrl;
  }
  await refreshAll({ skipStatusMessage: true });
  showStatusMessage('配置与面板已刷新', 'success');
}

async function refreshAll(options = {}) {
  const skipStatusMessage = options.skipStatusMessage;
  if (!state.config) {
    return;
  }
  setStatusIndicator('pending', '同步中...');
  const results = await Promise.all([
    renderPanels(),
    renderTables(),
    renderLists(),
    refreshModules()
  ]);
  const hasError = results.some(Boolean);
  setStatusIndicator(hasError ? 'error' : 'success', hasError ? '部分模块加载失败' : 'API 已同步');
  if (!skipStatusMessage) {
    showStatusMessage(
      hasError ? '部分模块加载失败，请检查凭证或网络。' : '所有面板均已同步',
      hasError ? 'warning' : 'success'
    );
  }
}

function updateCredentialSummary() {
  if (!elements.credentialSummary) {
    return;
  }
  const { username, password } = state.auth || {};
  if (username && password) {
    const masked = '*'.repeat(Math.min(password.length, 6));
    elements.credentialSummary.textContent =
      '当前凭证：' + username + ' / ' + masked;
  } else {
    elements.credentialSummary.textContent = '当前凭证：未设置';
  }
  if (elements.apiBaseUrl) {
    elements.apiBaseUrl.textContent = state.baseUrl;
  }
}

function setStatusIndicator(status, label) {
  if (!elements.statusIndicator) {
    return;
  }
  elements.statusIndicator.classList.remove('pending', 'success', 'error');
  elements.statusIndicator.classList.add(status);
  elements.statusIndicator.textContent = label;
}

function showStatusMessage(message, level = 'info') {
  if (!elements.statusMessage) {
    return;
  }
  elements.statusMessage.textContent = message;
  elements.statusMessage.className = 'status-message ' + level;
  clearTimeout(statusTimer);
  statusTimer = setTimeout(() => {
    if (elements.statusMessage) {
      elements.statusMessage.className = 'status-message';
    }
  }, 6000);
}

function makeApiUrl(path) {
  if (!path) {
    return '';
  }
  const base = (state.baseUrl || '').replace(/\/$/, '');
  const suffix = path.startsWith('/') ? path : '/' + path;
  return base + suffix;
}

function getBasicAuthHeader() {
  const { username, password } = state.auth || {};
  if (!username || !password) {
    return null;
  }
  try {
    return 'Basic ' + btoa(username + ':' + password);
  } catch (error) {
    return null;
  }
}

async function authenticatedFetch(url, options = {}) {
  const headers = new Headers(options.headers || {});
  const auth = getBasicAuthHeader();
  if (auth) {
    headers.set('Authorization', auth);
  }
  if (!headers.has('Accept')) {
    headers.set('Accept', 'application/json');
  }
  const response = await fetch(url, { ...options, headers });
  if (!response.ok) {
    let message = 'HTTP ' + response.status;
    try {
      const payload = await response.clone().json();
      message = payload?.error || payload?.message || message;
    } catch (err) {
      // ignore
    }
    throw new Error(message);
  }
  return response;
}

function getValueByPath(obj, path) {
  if (!path) {
    return obj;
  }
  return path.split('.').reduce((current, key) => {
    if (current && typeof current === 'object') {
      return current[key];
    }
    return undefined;
  }, obj);
}

function formatMetric(value, precision = 0, suffix = '') {
  if (value === undefined || value === null) {
    return '--';
  }
  if (typeof value === 'string') {
    return value.trim() ? value : '--';
  }
  const numeric = Number(value);
  if (!Number.isFinite(numeric)) {
    return String(value);
  }
  const formatted = numeric.toFixed(precision ?? 0);
  return suffix ? formatted + suffix : formatted;
}

function clamp(value, min, max) {
  const numeric = Number(value);
  if (Number.isNaN(numeric)) {
    return min;
  }
  return Math.min(Math.max(numeric, min), max);
}

function expandColors(colors, length) {
  if (!colors || !colors.length) {
    return Array.from({ length }, () => 'rgba(37, 99, 235, 0.75)');
  }
  return Array.from({ length }, (_, index) => colors[index % colors.length]);
}

async function renderPanels() {
  if (!elements.dashboard) {
    return '面板容器缺失';
  }
  const panels = state.config?.panels || [];
  elements.dashboard.innerHTML = '';
  if (!panels.length) {
    elements.dashboard.innerHTML = '<p class="panel-loading">暂无面板配置</p>';
    return null;
  }
  const results = await Promise.all(panels.map(async (panel, index) => {
    const panelEl = document.createElement('article');
    panelEl.className = 'dashboard-panel';
    const header = document.createElement('div');
    header.className = 'panel-header';
    const titleGroup = document.createElement('div');
    const title = document.createElement('h3');
    title.textContent = panel.title || '未命名面板';
    const description = document.createElement('p');
    description.textContent = panel.description || '';
    titleGroup.appendChild(title);
    titleGroup.appendChild(description);
    header.appendChild(titleGroup);
    panelEl.appendChild(header);
    let cardGrid;
    if (panel.cards && panel.cards.length) {
      cardGrid = document.createElement('div');
      cardGrid.className = 'card-grid';
      panelEl.appendChild(cardGrid);
    }
    try {
      const response = await authenticatedFetch(makeApiUrl(panel.endpoint));
      const payload = await response.json();
      if (cardGrid) {
        panel.cards.forEach(card => {
          const metric = document.createElement('div');
          metric.className = 'metric-card';
          const label = document.createElement('span');
          label.textContent = card.label || '未命名';
          const valueNode = document.createElement('strong');
          valueNode.textContent = formatMetric(
            getValueByPath(payload, card.valuePath),
            card.precision,
            card.suffix || ''
          );
          metric.appendChild(label);
          metric.appendChild(valueNode);
          cardGrid.appendChild(metric);
        });
      }
      if (panel.chart && panel.chart.dataPath) {
        const chartWrapper = document.createElement('div');
        chartWrapper.className = 'chart-wrapper';
        const chartTitle = document.createElement('p');
        chartTitle.className = 'chart-title';
        chartTitle.textContent = panel.chart.title || '';
        const canvas = document.createElement('canvas');
        chartWrapper.appendChild(chartTitle);
        chartWrapper.appendChild(canvas);
        panelEl.appendChild(chartWrapper);
        try {
          const ctx = canvas.getContext('2d');
          if (ctx) {
            const chartKey = panel.id || `panel-${index}`;
            if (state.charts[chartKey]) {
              state.charts[chartKey].destroy();
            }
            const chartData = buildChartData(panel.chart, payload);
            state.charts[chartKey] = new Chart(ctx, {
              type: panel.chart.type || 'bar',
              data: {
                labels: chartData.labels,
                datasets: [{
                  label: panel.chart.title || '',
                  data: chartData.values,
                  backgroundColor: expandColors(panel.chart.colors, chartData.values.length),
                  borderColor: chartData.values.map(() => 'rgba(15, 23, 42, 0.15)'),
                  borderWidth: panel.chart.type === 'doughnut' ? 0 : 1
                }]
              },
              options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                  legend: {
                    display: (panel.chart.type || 'bar') !== 'bar',
                    position: 'bottom'
                  }
                },
                scales: (panel.chart.type || 'bar') === 'doughnut' ? undefined : {
                  y: {
                    beginAtZero: true
                  }
                }
              }
            });
          }
        } catch (chartError) {
          const chartErrorNode = document.createElement('p');
          chartErrorNode.className = 'panel-error';
          chartErrorNode.textContent = '图表加载失败：' + chartError.message;
          panelEl.appendChild(chartErrorNode);
        }
      }
      elements.dashboard.appendChild(panelEl);
      return null;
    } catch (error) {
      const errorNode = document.createElement('p');
      errorNode.className = 'panel-error';
      errorNode.textContent = '统计数据加载失败：' + error.message;
      panelEl.appendChild(errorNode);
      elements.dashboard.appendChild(panelEl);
      return error.message;
    }
  }));
  return results.find(Boolean) || null;
}

function buildChartData(chartConfig, payload) {
  const rawData = getValueByPath(payload, chartConfig.dataPath);
  let labels = Array.isArray(chartConfig.labels) ? chartConfig.labels.slice() : [];
  let values = [];
  if (Array.isArray(rawData)) {
    values = rawData.map(entry => Number(entry) || 0);
    if (!labels.length) {
      labels = values.map((_, index) => `数据 ${index + 1}`);
    }
  } else if (rawData && typeof rawData === 'object') {
    if (!labels.length) {
      labels = Object.keys(rawData);
    }
    values = labels.map(label => Number(rawData[label]) || 0);
  } else if (typeof rawData === 'number') {
    values = [rawData];
    if (!labels.length) {
      labels = ['值'];
    }
  }
  return { labels, values };
}

async function renderTables() {
  if (!elements.tables) {
    return '表格区域缺失';
  }
  const tables = state.config?.tables || [];
  elements.tables.innerHTML = '';
  if (!tables.length) {
    elements.tables.innerHTML = '<p class="panel-loading">暂无表格配置</p>';
    return null;
  }
  const results = await Promise.all(tables.map(async table => {
    const card = document.createElement('article');
    card.className = 'table-card';
    const title = document.createElement('h3');
    title.textContent = table.title || '未知表格';
    const description = document.createElement('p');
    description.textContent = table.description || '';
    card.appendChild(title);
    card.appendChild(description);
    const tableEl = document.createElement('table');
    const thead = document.createElement('thead');
    const headerRow = document.createElement('tr');
    (table.columns || []).forEach(column => {
      const th = document.createElement('th');
      th.textContent = column.header || '';
      headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);
    tableEl.appendChild(thead);
    const tbody = document.createElement('tbody');
    tableEl.appendChild(tbody);
    card.appendChild(tableEl);
    try {
      const response = await authenticatedFetch(makeApiUrl(table.endpoint));
      const payload = await response.json();
      const rows = Array.isArray(payload) ? payload : Object.values(payload || {});
      const displayRows = table.limit ? rows.slice(0, table.limit) : rows;
      if (!displayRows.length) {
        const emptyRow = document.createElement('tr');
        const emptyCell = document.createElement('td');
        emptyCell.colSpan = Math.max(table.columns?.length || 1, 1);
        emptyCell.textContent = '暂无数据';
        emptyRow.appendChild(emptyCell);
        tbody.appendChild(emptyRow);
      } else {
        displayRows.forEach(row => {
          const tr = document.createElement('tr');
          (table.columns || []).forEach(column => {
            const td = document.createElement('td');
            td.textContent = formatMetric(
              getValueByPath(row, column.key),
              column.precision,
              column.suffix || ''
            );
            tr.appendChild(td);
          });
          tbody.appendChild(tr);
        });
      }
      elements.tables.appendChild(card);
      return null;
    } catch (error) {
      const errorNode = document.createElement('p');
      errorNode.className = 'panel-error';
      errorNode.textContent = '表格加载失败：' + error.message;
      card.appendChild(errorNode);
      elements.tables.appendChild(card);
      return error.message;
    }
  }));
  return results.find(Boolean) || null;
}

async function renderLists() {
  if (!elements.lists) {
    return '列表区域缺失';
  }
  const lists = state.config?.lists || [];
  elements.lists.innerHTML = '';
  if (!lists.length) {
    elements.lists.innerHTML = '<p class="panel-loading">暂无列表配置</p>';
    return null;
  }
  const results = await Promise.all(lists.map(async listConfig => {
    const card = document.createElement('article');
    card.className = 'list-card';
    const heading = document.createElement('h3');
    heading.textContent = listConfig.title || '列表';
    const description = document.createElement('p');
    description.textContent = listConfig.description || '';
    card.appendChild(heading);
    card.appendChild(description);
    try {
      const response = await authenticatedFetch(makeApiUrl(listConfig.endpoint));
      const payload = await response.json();
      const entries = normalizeListPayload(payload);
      if (!entries.length) {
        const emptyNode = document.createElement('p');
        emptyNode.className = 'panel-loading';
        emptyNode.textContent = '暂无数据';
        card.appendChild(emptyNode);
      } else {
        const listNode = document.createElement('ul');
        listNode.className = 'pass-rate-list';
        const sortOrder = listConfig.sort === 'asc' ? 1 : -1;
        entries.sort((a, b) => sortOrder * (a.value - b.value));
        entries.forEach(entry => {
          const line = document.createElement('li');
          line.className = 'list-line';
          const info = document.createElement('div');
          info.className = 'list-info';
          const suffix = listConfig.valueSuffix || '';
          info.innerHTML = `<strong>${entry.label}</strong><span>${formatMetric(entry.value, 2, suffix)}</span>`;
          const progress = document.createElement('div');
          progress.className = 'progress-track';
          const fill = document.createElement('div');
          fill.className = 'progress-fill';
          fill.style.width = clamp(entry.value, 0, 100) + '%';
          progress.appendChild(fill);
          line.appendChild(info);
          line.appendChild(progress);
          listNode.appendChild(line);
        });
        card.appendChild(listNode);
      }
      elements.lists.appendChild(card);
      return null;
    } catch (error) {
      const errorNode = document.createElement('p');
      errorNode.className = 'panel-error';
      errorNode.textContent = '列表加载失败：' + error.message;
      card.appendChild(errorNode);
      elements.lists.appendChild(card);
      return error.message;
    }
  }));
  return results.find(Boolean) || null;
}

function normalizeListPayload(payload) {
  if (!payload) {
    return [];
  }
  if (Array.isArray(payload)) {
    return payload.map((item, index) => ({
      label: item.label || item.name || `项 ${index + 1}`,
      value: Number(item.value ?? item.score ?? item.passRate ?? 0) || 0
    }));
  }
  if (typeof payload === 'object') {
    return Object.entries(payload).map(([key, value]) => ({
      label: key,
      value: Number(value) || 0
    }));
  }
  return [];
}

async function refreshModules() {
  const results = await Promise.allSettled([
    authenticatedFetch(makeApiUrl('/api/students')).then(res => res.json()),
    authenticatedFetch(makeApiUrl('/api/courses')).then(res => res.json()),
    authenticatedFetch(makeApiUrl('/api/scores')).then(res => res.json())
  ]);

  const [studentsResult, coursesResult, scoresResult] = results;
  const errors = [];

  if (studentsResult.status === 'fulfilled') {
    state.students = studentsResult.value;
  } else {
    state.students = [];
    errors.push('学生模块加载失败');
  }

  if (coursesResult.status === 'fulfilled') {
    state.courses = coursesResult.value;
  } else {
    state.courses = [];
    errors.push('课程模块加载失败');
  }

  if (scoresResult.status === 'fulfilled') {
    state.scores = scoresResult.value;
  } else {
    state.scores = [];
    errors.push('成绩模块加载失败');
  }

  renderStudentModule();
  renderCourseModule();
  renderScoreModule();

  return errors.length ? errors.join('；') : null;
}

function renderStudentModule() {
  if (!elements.studentsTableBody) {
    return;
  }
  elements.studentsTableBody.innerHTML = '';
  const fragment = document.createDocumentFragment();
  if (!state.students.length) {
    const emptyRow = document.createElement('tr');
    const emptyCell = document.createElement('td');
    emptyCell.colSpan = 5;
    emptyCell.textContent = '暂无学生数据';
    emptyRow.appendChild(emptyCell);
    fragment.appendChild(emptyRow);
  } else {
    state.students.forEach(student => {
      const tr = document.createElement('tr');
      tr.innerHTML = `<td>${student.studentNumber || '--'}</td><td>${student.name || '--'}</td><td>${student.clazz || '--'}</td><td>${student.major || '--'}</td>`;
      const actionTd = document.createElement('td');
      actionTd.className = 'text-end';
      const actionWrapper = document.createElement('div');
      actionWrapper.className = 'action-buttons';
      actionWrapper.appendChild(buildActionButton('编辑', 'secondary', () => fillStudentForm(student)));
      actionWrapper.appendChild(buildActionButton('删除', 'danger', () => deleteStudent(student.id)));
      actionTd.appendChild(actionWrapper);
      tr.appendChild(actionTd);
      fragment.appendChild(tr);
    });
  }
  elements.studentsTableBody.appendChild(fragment);
}

function fillStudentForm(student) {
  if (!student) {
    return;
  }
  elements.studentIdInput.value = student.id || '';
  elements.studentNumberInput.value = student.studentNumber || '';
  elements.studentNameInput.value = student.name || '';
  elements.studentClazzInput.value = student.clazz || '';
  elements.studentMajorInput.value = student.major || '';
}

function clearStudentForm() {
  elements.studentForm?.reset();
  if (elements.studentIdInput) {
    elements.studentIdInput.value = '';
  }
}

function renderCourseModule() {
  if (!elements.coursesTableBody) {
    return;
  }
  elements.coursesTableBody.innerHTML = '';
  const fragment = document.createDocumentFragment();
  if (!state.courses.length) {
    const emptyRow = document.createElement('tr');
    const emptyCell = document.createElement('td');
    emptyCell.colSpan = 4;
    emptyCell.textContent = '暂无课程数据';
    emptyRow.appendChild(emptyCell);
    fragment.appendChild(emptyRow);
  } else {
    state.courses.forEach(course => {
      const tr = document.createElement('tr');
      tr.innerHTML = `<td>${course.name || '--'}</td><td>${course.credit ?? '--'}</td><td>${course.teacherName || '--'}</td>`;
      const actionTd = document.createElement('td');
      actionTd.className = 'text-end';
      const actionWrapper = document.createElement('div');
      actionWrapper.className = 'action-buttons';
      actionWrapper.appendChild(buildActionButton('编辑', 'secondary', () => fillCourseForm(course)));
      actionWrapper.appendChild(buildActionButton('删除', 'danger', () => deleteCourse(course.id)));
      actionTd.appendChild(actionWrapper);
      tr.appendChild(actionTd);
      fragment.appendChild(tr);
    });
  }
  elements.coursesTableBody.appendChild(fragment);
}

function fillCourseForm(course) {
  if (!course) {
    return;
  }
  elements.courseIdInput.value = course.id || '';
  elements.courseNameInput.value = course.name || '';
  elements.courseCreditInput.value = course.credit ?? '';
  elements.courseTeacherInput.value = course.teacherName || '';
}

function clearCourseForm() {
  elements.courseForm?.reset();
  if (elements.courseIdInput) {
    elements.courseIdInput.value = '';
  }
}

function renderScoreModule() {
  if (!elements.scoresTableBody) {
    return;
  }
  elements.scoresTableBody.innerHTML = '';
  const fragment = document.createDocumentFragment();
  if (!state.scores.length) {
    const emptyRow = document.createElement('tr');
    const emptyCell = document.createElement('td');
    emptyCell.colSpan = 5;
    emptyCell.textContent = '暂无成绩数据';
    emptyRow.appendChild(emptyCell);
    fragment.appendChild(emptyRow);
  } else {
    state.scores.forEach(score => {
      const tr = document.createElement('tr');
      const studentName = score.student ? `${score.student.name || '--'} ${score.student.studentNumber ? `(${score.student.studentNumber})` : ''}`.trim() : '--';
      const courseName = score.course ? score.course.name || '--' : '--';
      tr.innerHTML = `<td>${score.id || '--'}</td><td>${studentName}</td><td>${courseName}</td><td>${formatMetric(score.value, 1)}</td>`;
      const actionTd = document.createElement('td');
      actionTd.className = 'text-end';
      const actionWrapper = document.createElement('div');
      actionWrapper.className = 'action-buttons';
      actionWrapper.appendChild(buildActionButton('编辑', 'secondary', () => fillScoreForm(score)));
      actionWrapper.appendChild(buildActionButton('删除', 'danger', () => deleteScore(score.id)));
      actionTd.appendChild(actionWrapper);
      tr.appendChild(actionTd);
      fragment.appendChild(tr);
    });
  }
  elements.scoresTableBody.appendChild(fragment);
  populateScoreSelectors();
}

function fillScoreForm(score) {
  if (!score) {
    return;
  }
  elements.scoreIdInput.value = score.id || '';
  if (score.student?.id) {
    elements.scoreStudentSelect.value = score.student.id;
  }
  if (score.course?.id) {
    elements.scoreCourseSelect.value = score.course.id;
  }
  elements.scoreValueInput.value = score.value ?? '';
}

function clearScoreForm() {
  elements.scoreForm?.reset();
  if (elements.scoreIdInput) {
    elements.scoreIdInput.value = '';
  }
}

function populateScoreSelectors() {
  populateSelect(
    elements.scoreStudentSelect,
    state.students,
    student => `${student.name || student.studentNumber || '学生'} (${student.studentNumber || ''})`,
    '请选择学生'
  );
  populateSelect(
    elements.scoreCourseSelect,
    state.courses,
    course => course.name || '课程',
    '请选择课程'
  );
}

function populateSelect(select, items, labelFn, placeholder) {
  if (!select) {
    return;
  }
  const previous = select.value;
  select.innerHTML = '';
  if (placeholder) {
    const placeholderOption = document.createElement('option');
    placeholderOption.value = '';
    placeholderOption.textContent = placeholder;
    placeholderOption.disabled = true;
    placeholderOption.selected = true;
    select.appendChild(placeholderOption);
  }
  (items || []).forEach(item => {
    if (item == null || item.id == null) {
      return;
    }
    const option = document.createElement('option');
    option.value = item.id;
    option.textContent = labelFn(item);
    select.appendChild(option);
  });
  if (previous) {
    const match = Array.from(select.options).find(opt => opt.value === previous);
    if (match) {
      select.value = previous;
    }
  }
}

async function handleCredentialSubmit(event) {
  event.preventDefault();
  if (!elements.usernameInput || !elements.passwordInput) {
    return;
  }
  const username = elements.usernameInput.value.trim();
  const password = elements.passwordInput.value.trim();
  if (!username || !password) {
    showStatusMessage('请填写用户名和密码', 'warning');
    return;
  }
  state.auth = { username, password };
  updateCredentialSummary();
  await loadConfig();
}

async function handleStudentSubmit(event) {
  event.preventDefault();
  if (
    !elements.studentNumberInput ||
    !elements.studentNameInput ||
    !elements.studentClazzInput ||
    !elements.studentMajorInput
  ) {
    return;
  }
  const payload = {
    studentNumber: elements.studentNumberInput.value.trim(),
    name: elements.studentNameInput.value.trim(),
    clazz: elements.studentClazzInput.value.trim(),
    major: elements.studentMajorInput.value.trim()
  };
  if (!payload.studentNumber || !payload.name || !payload.clazz || !payload.major) {
    showStatusMessage('请完整填写学生信息', 'warning');
    return;
  }
  const studentId = elements.studentIdInput?.value;
  const endpoint = studentId ? `/api/students/${studentId}` : '/api/students';
  try {
    await authenticatedFetch(makeApiUrl(endpoint), {
      method: studentId ? 'PUT' : 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    showStatusMessage('学生信息已保存', 'success');
    clearStudentForm();
    await refreshAll({ skipStatusMessage: true });
  } catch (error) {
    showStatusMessage(error.message, 'error');
  }
}

async function handleCourseSubmit(event) {
  event.preventDefault();
  if (!elements.courseNameInput || !elements.courseCreditInput || !elements.courseTeacherInput) {
    return;
  }
  const payload = {
    name: elements.courseNameInput.value.trim(),
    credit: parseFloat(elements.courseCreditInput.value),
    teacherName: elements.courseTeacherInput.value.trim()
  };
  if (!payload.name || Number.isNaN(payload.credit) || !payload.teacherName) {
    showStatusMessage('请完整填写课程信息', 'warning');
    return;
  }
  const courseId = elements.courseIdInput?.value;
  const endpoint = courseId ? `/api/courses/${courseId}` : '/api/courses';
  try {
    await authenticatedFetch(makeApiUrl(endpoint), {
      method: courseId ? 'PUT' : 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    showStatusMessage('课程信息已保存', 'success');
    clearCourseForm();
    await refreshAll({ skipStatusMessage: true });
  } catch (error) {
    showStatusMessage(error.message, 'error');
  }
}

async function handleScoreSubmit(event) {
  event.preventDefault();
  if (!elements.scoreStudentSelect || !elements.scoreCourseSelect || !elements.scoreValueInput) {
    return;
  }
  const studentId = elements.scoreStudentSelect.value;
  const courseId = elements.scoreCourseSelect.value;
  const scoreValue = parseFloat(elements.scoreValueInput.value);
  if (!studentId || !courseId || Number.isNaN(scoreValue)) {
    showStatusMessage('请选择学生/课程并填写成绩', 'warning');
    return;
  }
  const payload = {
    student: { id: Number(studentId) },
    course: { id: Number(courseId) },
    value: scoreValue
  };
  const scoreId = elements.scoreIdInput?.value;
  try {
    if (scoreId) {
      await authenticatedFetch(makeApiUrl(`/api/scores/${scoreId}?value=${scoreValue}`), {
        method: 'PUT'
      });
      showStatusMessage('成绩已更新', 'success');
    } else {
      await authenticatedFetch(makeApiUrl('/api/scores'), {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      showStatusMessage('成绩已提交', 'success');
    }
    clearScoreForm();
    await refreshAll({ skipStatusMessage: true });
  } catch (error) {
    showStatusMessage(error.message, 'error');
  }
}

async function deleteStudent(id) {
  if (!id) {
    return;
  }
  try {
    await authenticatedFetch(makeApiUrl(`/api/students/${id}`), { method: 'DELETE' });
    showStatusMessage('学生已删除', 'success');
    await refreshAll({ skipStatusMessage: true });
  } catch (error) {
    showStatusMessage(error.message, 'error');
  }
}

async function deleteCourse(id) {
  if (!id) {
    return;
  }
  try {
    await authenticatedFetch(makeApiUrl(`/api/courses/${id}`), { method: 'DELETE' });
    showStatusMessage('课程已删除', 'success');
    await refreshAll({ skipStatusMessage: true });
  } catch (error) {
    showStatusMessage(error.message, 'error');
  }
}

async function deleteScore(id) {
  if (!id) {
    return;
  }
  try {
    await authenticatedFetch(makeApiUrl(`/api/scores/${id}`), { method: 'DELETE' });
    showStatusMessage('成绩已删除', 'success');
    await refreshAll({ skipStatusMessage: true });
  } catch (error) {
    showStatusMessage(error.message, 'error');
  }
}

async function handleExcelImport(event, endpoint, label) {
  event.preventDefault();
  const form = event.target;
  const fileInput = form.querySelector('input[type="file"]');
  const file = fileInput?.files?.[0];
  if (!file) {
    showStatusMessage('请先选择文件', 'warning');
    return;
  }
  const formData = new FormData();
  formData.append('file', file);
  try {
    const response = await authenticatedFetch(makeApiUrl(endpoint), {
      method: 'POST',
      body: formData
    });
    const result = await response.json();
    const successMessages = (result.successMessages || []).join('; ');
    const errorMessages = (result.errorMessages || []).join('; ');
    let summary = `${label}完成：${result.successCount ?? 0} 成功，${result.errorCount ?? 0} 失败。`;
    if (successMessages) {
      summary += ` 成功项：${successMessages}。`;
    }
    if (errorMessages) {
      summary += ` 失败项：${errorMessages}。`;
    }
    if (elements.excelStatus) {
      elements.excelStatus.textContent = summary;
    }
    showStatusMessage(`${label}结束`, 'success');
    await refreshAll({ skipStatusMessage: true });
  } catch (error) {
    if (elements.excelStatus) {
      elements.excelStatus.textContent = `${label}失败：${error.message}`;
    }
    showStatusMessage(error.message, 'error');
  } finally {
    form.reset();
  }
}

async function handleExport(button) {
  const endpoint = button.dataset.endpoint;
  const filename = button.dataset.filename || 'export.xlsx';
  if (!endpoint) {
    return;
  }
  try {
    const response = await authenticatedFetch(makeApiUrl(endpoint));
    const blob = await response.blob();
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
    showStatusMessage(`${filename} 下载完成`, 'success');
  } catch (error) {
    showStatusMessage(error.message, 'error');
  }
}

function buildActionButton(label, variant, handler) {
  const button = document.createElement('button');
  button.type = 'button';
  button.textContent = label;
  button.classList.add(variant);
  button.addEventListener('click', handler);
  return button;
}
