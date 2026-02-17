import { ensureRoleAccess, bindUserBar } from './auth.js';

import { formatMetric } from './utils.js';

const elements = {
  info: document.getElementById('studentInfo'),
  scoresBody: document.getElementById('studentScoresBody'),
  recommendationList: document.getElementById('studentRecommendationList'),
  trendChart: document.getElementById('studentTrendChart')
};

async function loadSelf() {
  const user = await ensureRoleAccess('student');
  if (!user) {
    return null;
  }
  bindUserBar(user);
  const res = await fetch('/api/student/self', { credentials: 'include' });
  if (!res.ok) {
    throw new Error('无法读取学生信息');
  }
  return res.json();
}

async function loadScores() {
  const res = await fetch('/api/student/self/scores', { credentials: 'include' });
  if (!res.ok) {
    throw new Error('无法读取成绩');
  }
  return res.json();
}

async function loadRecommendations() {
  const res = await fetch('/api/student/self/recommendations', { credentials: 'include' });
  if (!res.ok) {
    throw new Error('无法读取推荐');
  }
  return res.json();
}

async function loadTrend() {
  const res = await fetch('/api/student/self/trend', { credentials: 'include' });
  if (!res.ok) {
    throw new Error('无法读取趋势');
  }
  return res.json();
}

function renderInfo(info) {
  if (!elements.info) {
    return;
  }
  elements.info.innerHTML = `
    <ul>
      <li><span>姓名</span><strong>${info?.name || '--'}</strong></li>
      <li><span>学号</span><strong>${info?.studentNumber || '--'}</strong></li>
      <li><span>班级</span><strong>${info?.clazz || '--'}</strong></li>
      <li><span>专业</span><strong>${info?.major || '--'}</strong></li>
    </ul>
  `;
}

function renderScores(scores) {
  if (!elements.scoresBody) {
    return;
  }
  elements.scoresBody.innerHTML = '';
  if (!scores.length) {
    elements.scoresBody.innerHTML = '<tr><td colspan="5" class="panel-loading">暂无成绩</td></tr>';
    return;
  }
  scores.forEach(score => {
    const tr = document.createElement('tr');
    const courseName = score.course?.name || '--';
    const examName = score.exam?.name || '--';
    const recordedAt = score.recordedAt || '--';
    tr.innerHTML = `
      <td>${courseName}</td>
      <td>${examName}</td>
      <td>${formatMetric(score.value, 1)}</td>
      <td>${recordedAt}</td>
    `;
    elements.scoresBody.appendChild(tr);
  });
}

function renderRecommendations(data) {
  if (!elements.recommendationList) {
    return;
  }
  const items = data?.recommendations || [];
  if (!items.length) {
    elements.recommendationList.innerHTML = '<p class="panel-loading">暂无推荐</p>';
    return;
  }
  elements.recommendationList.innerHTML = '';
  items.forEach(item => {
    const block = document.createElement('div');
    block.className = 'recommendation-item';
    const trendDelta = formatMetric(item.trendDelta, 1);
    block.innerHTML = `
      <header><strong>${item.courseName}</strong><span>${item.trend} ${trendDelta}</span></header>
      <p>${item.reason || '暂无推荐理由'}</p>
      <p class="muted">当前 ${formatMetric(item.currentScore, 1)}；班级均值 ${formatMetric(item.classAverage, 1)}；差值 ${formatMetric(item.differenceWithClassAvg, 1)}</p>
    `;
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
    elements.recommendationList.appendChild(block);
  });
}

function renderTrend(trend) {
  if (!elements.trendChart) {
    return;
  }
  const labels = (trend || []).map(item => item.examName || item.examDate || '--');
  const values = (trend || []).map(item => item.average ?? 0);
  if (!labels.length) {
    const fallback = document.createElement('p');
    fallback.className = 'panel-loading';
    fallback.textContent = '暂无趋势数据';
    elements.trendChart.replaceWith(fallback);
    return;
  }
  const ctx = elements.trendChart.getContext('2d');
  if (!ctx) {
    return;
  }
  new Chart(ctx, {
    type: 'line',
    data: {
      labels,
      datasets: [{
        label: '平均分',
        data: values,
        borderColor: '#2563eb',
        backgroundColor: 'rgba(37, 99, 235, 0.12)',
        borderWidth: 2,
        fill: true,
        tension: 0.35
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { position: 'bottom' }
      },
      scales: {
        y: { beginAtZero: true }
      }
    }
  });
}

(async () => {
  try {
    const info = await loadSelf();
    renderInfo(info);
    const [scores, recommendations, trend] = await Promise.all([loadScores(), loadRecommendations(), loadTrend()]);
    renderScores(scores);
    renderRecommendations(recommendations);
    renderTrend(trend);
  } catch (error) {
    if (elements.recommendationList) {
      elements.recommendationList.innerHTML = `<p class="panel-error">${error.message}</p>`;
    }
  }
})();
