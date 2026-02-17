
import { makeApiUrl as buildApiUrl, authenticatedFetch as baseAuthenticatedFetch } from './api.js';
import { ensureRoleAccess, bindUserBar } from './auth.js';
import {
  getValueByPath as getValueByPathInternal,
  formatMetric as formatMetricInternal,
  clamp as clampInternal,
  expandColors as expandColorsInternal
} from './utils.js';

const state = {
  baseUrl: 'http://localhost:8080',
  auth: null,
  config: null,
  charts: {},
  students: [],
  courses: [],
  exams: [],
  scores: [],
  materials: [],
  teaching: {
    overview: null,
    flags: [],
    recommendations: null,
    selectedStudentId: null
  }
};

let elements;
let statusTimer;
let pageMode = 'dashboard';
let currentUser = null;

document.addEventListener('DOMContentLoaded', () => {
  pageMode = document.body?.dataset?.page || 'dashboard';
  elements = {
    statusIndicator: document.getElementById('statusIndicator'),
    statusMessage: document.getElementById('statusMessage'),
    apiBaseUrl: document.getElementById('apiBaseUrl'),
    dashboard: document.getElementById('dashboard'),
    tables: document.getElementById('tables'),
    lists: document.getElementById('lists'),
    refreshButton: document.getElementById('refreshButton'),
    studentForm: document.getElementById('studentForm'),
    courseForm: document.getElementById('courseForm'),
    examForm: document.getElementById('examForm'),
    scoreForm: document.getElementById('scoreForm'),
    studentFormReset: document.getElementById('studentFormReset'),
    courseFormReset: document.getElementById('courseFormReset'),
    examFormReset: document.getElementById('examFormReset'),
    scoreFormReset: document.getElementById('scoreFormReset'),
    studentsTableBody: document.getElementById('studentsTableBody'),
    coursesTableBody: document.getElementById('coursesTableBody'),
    examsTableBody: document.getElementById('examsTableBody'),
    scoresTableBody: document.getElementById('scoresTableBody'),
    scoreStudentSelect: document.getElementById('scoreStudentSelect'),
    scoreCourseSelect: document.getElementById('scoreCourseSelect'),
    scoreIdInput: document.getElementById('scoreIdInput'),
    scoreValueInput: document.getElementById('scoreValueInput'),
    scoreExamSelect: document.getElementById('scoreExamSelect'),
    studentIdInput: document.getElementById('studentIdInput'),
    courseIdInput: document.getElementById('courseIdInput'),
    studentNumberInput: document.getElementById('studentNumberInput'),
    studentNameInput: document.getElementById('studentNameInput'),
    studentClazzInput: document.getElementById('studentClazzInput'),
    studentMajorInput: document.getElementById('studentMajorInput'),
    courseNameInput: document.getElementById('courseNameInput'),
    courseCreditInput: document.getElementById('courseCreditInput'),
    courseTeacherInput: document.getElementById('courseTeacherInput'),
    examIdInput: document.getElementById('examIdInput'),
    examNameInput: document.getElementById('examNameInput'),
    examDateInput: document.getElementById('examDateInput'),
    materialForm: document.getElementById('materialForm'),
    materialFormReset: document.getElementById('materialFormReset'),
    materialIdInput: document.getElementById('materialIdInput'),
    materialCourseInput: document.getElementById('materialCourseInput'),
    materialTitleInput: document.getElementById('materialTitleInput'),
    materialUrlInput: document.getElementById('materialUrlInput'),
    materialTypeSelect: document.getElementById('materialTypeSelect'),
    materialDifficultySelect: document.getElementById('materialDifficultySelect'),
    materialsTableBody: document.getElementById('materialsTableBody'),
    materialFilterInput: document.getElementById('materialFilterInput'),
    materialFilterClear: document.getElementById('materialFilterClear'),
    studentExcelForm: document.getElementById('studentExcelForm'),
    scoreExcelForm: document.getElementById('scoreExcelForm'),
    studentExcelFile: document.getElementById('studentExcelFile'),
    scoreExcelFile: document.getElementById('scoreExcelFile'),
    excelStatus: document.getElementById('excelStatus')
    ,
    insightClassInput: document.getElementById('insightClassInput'),
    insightOverviewButton: document.getElementById('insightOverviewButton'),
    insightFlagsButton: document.getElementById('insightFlagsButton'),
    insightStudentSelect: document.getElementById('insightStudentSelect'),
    insightRecommendationButton: document.getElementById('insightRecommendationButton'),
    overviewSummary: document.getElementById('overviewSummary'),
    flagsTableBody: document.getElementById('flagsTableBody'),
    recommendationList: document.getElementById('recommendationList')
    ,
    insightPromptInput: document.getElementById('insightPromptInput')
  };

  bindUI();
  init();
});

function bindUI() {
  elements.refreshButton?.addEventListener('click', () => loadConfig());
  elements.studentForm?.addEventListener('submit', handleStudentSubmit);
  elements.courseForm?.addEventListener('submit', handleCourseSubmit);
  elements.examForm?.addEventListener('submit', handleExamSubmit);
  elements.materialForm?.addEventListener('submit', handleMaterialSubmit);
  elements.materialFilterInput?.addEventListener('input', () => renderMaterialModule());
  elements.materialFilterClear?.addEventListener('click', () => {
    if (elements.materialFilterInput) {
      elements.materialFilterInput.value = '';
      renderMaterialModule();
    }
  });
  elements.scoreForm?.addEventListener('submit', handleScoreSubmit);
  elements.studentFormReset?.addEventListener('click', clearStudentForm);
  elements.courseFormReset?.addEventListener('click', clearCourseForm);
  elements.examFormReset?.addEventListener('click', clearExamForm);
  elements.materialFormReset?.addEventListener('click', clearMaterialForm);
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
  elements.insightOverviewButton?.addEventListener('click', () => loadTeachingOverview());
  elements.insightFlagsButton?.addEventListener('click', () => loadTeachingFlags());
  elements.insightRecommendationButton?.addEventListener('click', () => loadTeachingRecommendations());
  elements.insightStudentSelect?.addEventListener('change', event => {
    state.teaching.selectedStudentId = event.target.value || null;
  });
}

async function init() {
  setStatusIndicator('pending', '校验登录...');
  currentUser = await ensureRoleAccess(pageMode);
  if (!currentUser) {
    return;
  }
  bindUserBar(currentUser);
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
  const tasks = [];
  if (pageMode === 'dashboard') {
    tasks.push(renderPanels(), renderTables(), renderLists());
  }
  if (pageMode === 'dashboard' || pageMode === 'manage') {
    tasks.push(refreshModules());
  }
  if (pageMode === 'materials') {
    tasks.push(refreshMaterials());
  }
  const results = await Promise.all(tasks);
  const hasError = results.some(Boolean);
  setStatusIndicator(hasError ? 'error' : 'success', hasError ? '部分模块加载失败' : 'API 已同步');
  if (!skipStatusMessage) {
    showStatusMessage(
      hasError ? '部分模块加载失败，请检查凭证或网络。' : '所有面板均已同步',
      hasError ? 'warning' : 'success'
    );
  }
  if (pageMode === 'dashboard' || pageMode === 'analysis') {
    refreshTeachingInsights({ skipStatusMessage: true });
  }
}

async function refreshMaterials() {
  try {
    const response = await authenticatedFetch(makeApiUrl('/api/materials'));
    state.materials = await response.json();
    renderMaterialModule();
    return null;
  } catch (error) {
    state.materials = [];
    renderMaterialModule();
    return '资料库加载失败';
  }
}

function updateCredentialSummary() {
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
  return buildApiUrl(state.baseUrl, path);
}

async function authenticatedFetch(url, options = {}) {
  return baseAuthenticatedFetch(url, options, state.auth);
}

function getValueByPath(obj, path) {
  return getValueByPathInternal(obj, path);
}

function formatMetric(value, precision = 0, suffix = '') {
  return formatMetricInternal(value, precision, suffix);
}

function getInsightClassName() {
  return elements.insightClassInput?.value.trim() || '计算机1班';
}

function getInsightStudentId() {
  return state.teaching.selectedStudentId || elements.insightStudentSelect?.value || null;
}

function getInsightPromptText() {
  return elements.insightPromptInput?.value.trim() || null;
}

function clamp(value, min, max) {
  return clampInternal(value, min, max);
}

function expandColors(colors, length) {
  return expandColorsInternal(colors, length);
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
            if (!chartData.values.length) {
              const emptyNode = document.createElement('p');
              emptyNode.className = 'panel-loading';
              emptyNode.textContent = '暂无趋势数据';
              chartWrapper.replaceWith(emptyNode);
              return null;
            }
            const chartType = panel.chart.type || 'bar';
            const colors = expandColors(panel.chart.colors, chartData.values.length || 1);
            const dataset = {
              label: panel.chart.title || '',
              data: chartData.values,
              backgroundColor: chartType === 'line' ? 'rgba(37, 99, 235, 0.12)' : colors,
              borderColor: chartType === 'line' ? '#2563eb' : chartData.values.map(() => 'rgba(15, 23, 42, 0.15)'),
              borderWidth: chartType === 'doughnut' ? 0 : 2,
              fill: chartType === 'line',
              tension: chartType === 'line' ? 0.35 : 0
            };
            state.charts[chartKey] = new Chart(ctx, {
              type: chartType,
              data: {
                labels: chartData.labels,
                datasets: [dataset]
              },
              options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                  legend: {
                    display: chartType !== 'bar',
                    position: 'bottom'
                  }
                },
                scales: chartType === 'doughnut' ? undefined : {
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
        if (listConfig.sort !== 'none') {
          const sortOrder = listConfig.sort === 'asc' ? 1 : -1;
          entries.sort((a, b) => sortOrder * (a.value - b.value));
        }
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
    authenticatedFetch(makeApiUrl('/api/exams')).then(res => res.json()),
    authenticatedFetch(makeApiUrl('/api/scores')).then(res => res.json())
  ]);

  const [studentsResult, coursesResult, examsResult, scoresResult] = results;
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

  if (examsResult.status === 'fulfilled') {
    state.exams = examsResult.value;
  } else {
    state.exams = [];
    errors.push('考试模块加载失败');
  }

  renderStudentModule();
  renderCourseModule();
  renderExamModule();
  renderScoreModule();
  populateInsightStudentSelect();

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

function renderMaterialModule() {
  if (!elements.materialsTableBody) {
    return;
  }
  elements.materialsTableBody.innerHTML = '';
  const fragment = document.createDocumentFragment();
  const keyword = (elements.materialFilterInput?.value || '').trim().toLowerCase();
  const filtered = keyword
    ? state.materials.filter(material => {
        const haystack = `${material.courseKeyword || ''} ${material.title || ''}`.toLowerCase();
        return haystack.includes(keyword);
      })
    : state.materials;
  if (!filtered.length) {
    const emptyRow = document.createElement('tr');
    const emptyCell = document.createElement('td');
    emptyCell.colSpan = 5;
    emptyCell.textContent = keyword ? '未找到匹配资料' : '暂无资料数据';
    emptyRow.appendChild(emptyCell);
    fragment.appendChild(emptyRow);
  } else {
    filtered.forEach(material => {
      const tr = document.createElement('tr');
      tr.innerHTML = `<td>${material.courseKeyword || '--'}</td><td>${material.title || '--'}</td><td>${material.type || '--'}</td><td>${material.difficultyTag || '--'}</td>`;
      const actionTd = document.createElement('td');
      actionTd.className = 'text-end';
      const actionWrapper = document.createElement('div');
      actionWrapper.className = 'action-buttons';
      actionWrapper.appendChild(buildActionButton('编辑', 'secondary', () => fillMaterialForm(material)));
      actionWrapper.appendChild(buildActionButton('删除', 'danger', () => deleteMaterial(material.id)));
      actionTd.appendChild(actionWrapper);
      tr.appendChild(actionTd);
      fragment.appendChild(tr);
    });
  }
  elements.materialsTableBody.appendChild(fragment);
}

function fillMaterialForm(material) {
  if (!material) {
    return;
  }
  elements.materialIdInput.value = material.id || '';
  elements.materialCourseInput.value = material.courseKeyword || '';
  elements.materialTitleInput.value = material.title || '';
  elements.materialUrlInput.value = material.url || '';
  elements.materialTypeSelect.value = material.type || '';
  elements.materialDifficultySelect.value = material.difficultyTag || '';
}

function clearMaterialForm() {
  elements.materialForm?.reset();
  if (elements.materialIdInput) {
    elements.materialIdInput.value = '';
  }
}
function renderExamModule() {
  if (!elements.examsTableBody) {
    return;
  }
  elements.examsTableBody.innerHTML = '';
  const fragment = document.createDocumentFragment();
  if (!state.exams.length) {
    const emptyRow = document.createElement('tr');
    const emptyCell = document.createElement('td');
    emptyCell.colSpan = 3;
    emptyCell.textContent = '暂无考试数据';
    emptyRow.appendChild(emptyCell);
    fragment.appendChild(emptyRow);
  } else {
    state.exams.forEach(exam => {
      const tr = document.createElement('tr');
      const dateLabel = exam.examDate || '--';
      tr.innerHTML = `<td>${exam.name || '--'}</td><td>${dateLabel}</td>`;
      const actionTd = document.createElement('td');
      actionTd.className = 'text-end';
      const actionWrapper = document.createElement('div');
      actionWrapper.className = 'action-buttons';
      actionWrapper.appendChild(buildActionButton('编辑', 'secondary', () => fillExamForm(exam)));
      actionWrapper.appendChild(buildActionButton('删除', 'danger', () => deleteExam(exam.id)));
      actionTd.appendChild(actionWrapper);
      tr.appendChild(actionTd);
      fragment.appendChild(tr);
    });
  }
  elements.examsTableBody.appendChild(fragment);
}

function fillExamForm(exam) {
  if (!exam) {
    return;
  }
  elements.examIdInput.value = exam.id || '';
  elements.examNameInput.value = exam.name || '';
  elements.examDateInput.value = exam.examDate || '';
}

function clearExamForm() {
  elements.examForm?.reset();
  if (elements.examIdInput) {
    elements.examIdInput.value = '';
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
    emptyCell.colSpan = 6;
    emptyCell.textContent = '暂无成绩数据';
    emptyRow.appendChild(emptyCell);
    fragment.appendChild(emptyRow);
  } else {
    state.scores.forEach(score => {
      const tr = document.createElement('tr');
      const studentName = score.student ? `${score.student.name || '--'} ${score.student.studentNumber ? `(${score.student.studentNumber})` : ''}`.trim() : '--';
      const courseName = score.course ? score.course.name || '--' : '--';
      const examName = score.exam ? score.exam.name || '--' : '--';
      tr.innerHTML = `<td>${score.id || '--'}</td><td>${studentName}</td><td>${courseName}</td><td>${examName}</td><td>${formatMetric(score.value, 1)}</td>`;
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
  if (score.exam?.id && elements.scoreExamSelect) {
    elements.scoreExamSelect.value = score.exam.id;
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
  populateSelect(
    elements.scoreExamSelect,
    state.exams,
    exam => exam.name || '考试',
    '请选择考试'
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

async function handleExamSubmit(event) {
  event.preventDefault();
  if (!elements.examNameInput || !elements.examDateInput) {
    return;
  }
  const payload = {
    name: elements.examNameInput.value.trim(),
    examDate: elements.examDateInput.value
  };
  if (!payload.name || !payload.examDate) {
    showStatusMessage('请完整填写考试信息', 'warning');
    return;
  }
  const examId = elements.examIdInput?.value;
  const endpoint = examId ? `/api/exams/${examId}` : '/api/exams';
  try {
    await authenticatedFetch(makeApiUrl(endpoint), {
      method: examId ? 'PUT' : 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    showStatusMessage('考试信息已保存', 'success');
    clearExamForm();
    await refreshAll({ skipStatusMessage: true });
  } catch (error) {
    showStatusMessage(error.message, 'error');
  }
}

async function handleMaterialSubmit(event) {
  event.preventDefault();
  if (!elements.materialCourseInput || !elements.materialTitleInput || !elements.materialUrlInput) {
    return;
  }
  const payload = {
    courseKeyword: elements.materialCourseInput.value.trim(),
    title: elements.materialTitleInput.value.trim(),
    url: elements.materialUrlInput.value.trim(),
    type: elements.materialTypeSelect?.value || '',
    difficultyTag: elements.materialDifficultySelect?.value || ''
  };
  if (!payload.courseKeyword || !payload.title || !payload.url || !payload.type || !payload.difficultyTag) {
    showStatusMessage('请完整填写资料信息', 'warning');
    return;
  }
  const materialId = elements.materialIdInput?.value;
  const endpoint = materialId ? `/api/materials/${materialId}` : '/api/materials';
  try {
    await authenticatedFetch(makeApiUrl(endpoint), {
      method: materialId ? 'PUT' : 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    showStatusMessage('资料已保存', 'success');
    clearMaterialForm();
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
  const examId = elements.scoreExamSelect?.value;
  const scoreValue = parseFloat(elements.scoreValueInput.value);
  if (!studentId || !courseId || !examId || Number.isNaN(scoreValue)) {
    showStatusMessage('请选择学生/课程并填写成绩', 'warning');
    return;
  }
  const payload = {
    student: { id: Number(studentId) },
    course: { id: Number(courseId) },
    exam: { id: Number(examId) },
    value: scoreValue
  };
  const scoreId = elements.scoreIdInput?.value;
  try {
    if (scoreId) {
      await authenticatedFetch(makeApiUrl(`/api/scores/${scoreId}`), {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
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

async function deleteExam(id) {
  if (!id) {
    return;
  }
  try {
    await authenticatedFetch(makeApiUrl(`/api/exams/${id}`), { method: 'DELETE' });
    showStatusMessage('考试已删除', 'success');
    await refreshAll({ skipStatusMessage: true });
  } catch (error) {
    showStatusMessage(error.message, 'error');
  }
}

async function deleteMaterial(id) {
  if (!id) {
    return;
  }
  try {
    await authenticatedFetch(makeApiUrl(`/api/materials/${id}`), { method: 'DELETE' });
    showStatusMessage('资料已删除', 'success');
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

async function refreshTeachingInsights(options = {}) {
  await loadTeachingOverview(options);
  await loadTeachingFlags(options);
  if (getInsightStudentId()) {
    await loadTeachingRecommendations(options);
  }
}

async function loadTeachingOverview(options = {}) {
  const className = getInsightClassName();
  if (!className) {
    state.teaching.overview = null;
    renderTeachingOverview();
    if (!options.skipStatusMessage) {
      showStatusMessage('请先填写班级名称', 'warning');
    }
    return '班级未设置';
  }
  try {
    const response = await authenticatedFetch(makeApiUrl(`/api/analysis/classroom/${encodeURIComponent(className)}/overview`));
    state.teaching.overview = await response.json();
    renderTeachingOverview();
    if (!options.skipStatusMessage) {
      showStatusMessage('班级概览已刷新', 'success');
    }
    return null;
  } catch (error) {
    state.teaching.overview = null;
    renderTeachingOverview();
    const message = '班级概览加载失败：' + error.message;
    if (!options.skipStatusMessage) {
      showStatusMessage(message, 'error');
    }
    return message;
  }
}

async function loadTeachingFlags(options = {}) {
  const className = getInsightClassName();
  if (!className) {
    state.teaching.flags = [];
    renderTeachingFlags();
    return '班级未设置';
  }
  try {
    const response = await authenticatedFetch(makeApiUrl(`/api/analysis/classroom/${encodeURIComponent(className)}/student-flags`));
    state.teaching.flags = await response.json();
    renderTeachingFlags();
    if (!options.skipStatusMessage) {
      showStatusMessage('学生标签已刷新', 'success');
    }
    return null;
  } catch (error) {
    state.teaching.flags = [];
    renderTeachingFlags();
    const message = '学生标签加载失败：' + error.message;
    if (!options.skipStatusMessage) {
      showStatusMessage(message, 'error');
    }
    return message;
  }
}

async function loadTeachingRecommendations(options = {}) {
  const studentId = getInsightStudentId();
  if (!studentId) {
    state.teaching.recommendations = null;
    renderTeachingRecommendations();
    if (!options.skipStatusMessage) {
      showStatusMessage('请先选择学生', 'warning');
    }
    return '未选择学生';
  }
  try {
    const response = await authenticatedFetch(makeApiUrl(`/api/analysis/student/${studentId}/recommendations`));
    state.teaching.recommendations = await response.json();
    renderTeachingRecommendations();
    if (!options.skipStatusMessage) {
      showStatusMessage('个性化推荐已刷新', 'success');
    }
    return null;
  } catch (error) {
    state.teaching.recommendations = null;
    renderTeachingRecommendations();
    const message = '推荐资料加载失败：' + error.message;
    if (!options.skipStatusMessage) {
      showStatusMessage(message, 'error');
    }
    return message;
  }
}

function renderTeachingOverview() {
  const container = elements.overviewSummary;
  if (!container) {
    return;
  }
  const data = state.teaching.overview;
  if (!data) {
    container.innerHTML = '<p class="panel-loading">暂无数据</p>';
    return;
  }
  const metricsList = document.createElement('ul');
  const metrics = [
    { label: '班级平均分', value: formatMetric(data.classAverage, 2) },
    { label: '整体及格率', value: formatMetric(data.overallPassRate, 2, '%') },
    { label: '趋势', value: data.trend || '--' },
    { label: '风险学生比例', value: formatMetric(data.riskStudentRatio, 2, '%') }
  ];
  metricsList.innerHTML = metrics.map(item => `<li><span>${item.label}</span><strong>${item.value}</strong></li>`).join('');
  container.innerHTML = '';
  container.appendChild(metricsList);
  if (Array.isArray(data.summary) && data.summary.length) {
    const summaryList = document.createElement('ul');
    data.summary.forEach(text => {
      const li = document.createElement('li');
      li.innerHTML = `<span>${text}</span>`;
      summaryList.appendChild(li);
    });
    container.appendChild(summaryList);
  }
  const passRates = data.subjectPassRates || {};
  if (Object.keys(passRates).length) {
    const passList = document.createElement('ul');
    Object.entries(passRates).forEach(([course, rate]) => {
      const li = document.createElement('li');
      li.innerHTML = `<span>${course}</span><strong>${formatMetric(rate, 2, '%')}</strong>`;
      passList.appendChild(li);
    });
    container.appendChild(passList);
  }
}

function renderTeachingFlags() {
  const tbody = elements.flagsTableBody;
  if (!tbody) {
    return;
  }
  const rows = state.teaching.flags || [];
  if (!rows.length) {
    tbody.innerHTML = '<tr><td colspan="5" class="panel-loading">暂无数据</td></tr>';
    return;
  }
  tbody.innerHTML = '';
  rows.forEach(row => {
    const tr = document.createElement('tr');
    const rankDisplay = `${row.currentRank ?? '--'} / ${row.previousRank ?? '--'}`;
    tr.innerHTML = `
      <td>${row.studentName || '--'}</td>
      <td>${formatMetric(row.currentAverage, 2)}</td>
      <td>${row.rankTrend || '--'}</td>
      <td>${rankDisplay}</td>
      <td>${row.tag || '--'}</td>
    `;
    tbody.appendChild(tr);
  });
}

function renderTeachingRecommendations() {
  const container = elements.recommendationList;
  if (!container) {
    return;
  }
  const data = state.teaching.recommendations;
  if (!data || !Array.isArray(data.recommendations) || !data.recommendations.length) {
    container.innerHTML = '<p class="panel-loading">暂无数据</p>';
    return;
  }
  container.innerHTML = '';
  data.recommendations.forEach(item => {
    const block = document.createElement('div');
    block.className = 'recommendation-item';
    const header = document.createElement('header');
    const trendDelta = formatMetric(item.trendDelta, 1);
    header.innerHTML = `<strong>${item.courseName}</strong><span>${item.trend} ${trendDelta}</span>`;
    const reason = document.createElement('p');
    reason.textContent = item.reason || '暂无推荐理由';
    const stats = document.createElement('p');
    stats.className = 'muted';
    stats.textContent = `当前 ${formatMetric(item.currentScore, 1)}；班级均值 ${formatMetric(item.classAverage, 1)}；差值 ${formatMetric(item.differenceWithClassAvg, 1)}`;
    block.appendChild(header);
    block.appendChild(reason);
    block.appendChild(stats);
    const materials = Array.isArray(item.materials) ? item.materials : [];
    if (materials.length) {
      const list = document.createElement('ul');
      materials.forEach(material => {
        const li = document.createElement('li');
        li.innerHTML = `<a href="${material.url}" target="_blank" rel="noreferrer noopener">${material.title}</a> <span>${material.type} / ${material.difficulty}</span>`;
        list.appendChild(li);
      });
      block.appendChild(list);
    }
    container.appendChild(block);
  });
}

function populateInsightStudentSelect() {
  const select = elements.insightStudentSelect;
  if (!select) {
    return;
  }
  const previous = select.value;
  select.innerHTML = '';
  const placeholder = document.createElement('option');
  placeholder.value = '';
  placeholder.textContent = '请选择学生';
  placeholder.disabled = true;
  placeholder.selected = true;
  select.appendChild(placeholder);
  state.students.forEach(student => {
    if (!student || student.id == null) {
      return;
    }
    const option = document.createElement('option');
    option.value = student.id;
    option.textContent = `${student.name || student.studentNumber || '学生'} (${student.studentNumber || ''})`;
    select.appendChild(option);
  });
  if (previous) {
    const match = Array.from(select.options).find(opt => opt.value === previous);
    if (match) {
      select.value = previous;
      state.teaching.selectedStudentId = previous;
      return;
    }
  }
  const firstReal = state.students.find(s => s?.id != null);
  if (firstReal) {
    select.value = firstReal.id;
    state.teaching.selectedStudentId = String(firstReal.id);
  } else {
    select.value = '';
    state.teaching.selectedStudentId = null;
  }
}

async function attachRecommendationReasons(items) {
  if (!items.length) {
    return;
  }
  const promptOverride = getInsightPromptText();
  const requests = items.map(item => {
    const payload = {
      studentId: state.teaching.recommendations?.studentId,
      course: item.courseName,
      trend: item.trend,
      score: item.currentScore,
      classAvg: item.classAverage,
      differenceWithClassAvg: item.differenceWithClassAvg
      ,
      promptOverride
    };
    return authenticatedFetch(makeApiUrl('/api/analysis/llm/reason'), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
      .then(res => res.json())
      .then(json => {
        if (promptOverride || !item.reason) {
          item.reason = (json?.reason || item.reason);
        }
      })
      .catch(() => {
        // fallback to existing reason if call fails
      });
  });
  await Promise.all(requests);
}

function buildActionButton(label, variant, handler) {
  const button = document.createElement('button');
  button.type = 'button';
  button.textContent = label;
  button.classList.add(variant);
  button.addEventListener('click', handler);
  return button;
}
