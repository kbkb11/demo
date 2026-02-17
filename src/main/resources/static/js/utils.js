export function getValueByPath(obj, path) {
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

export function formatMetric(value, precision = 0, suffix = '') {
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

export function clamp(value, min, max) {
  const numeric = Number(value);
  if (Number.isNaN(numeric)) {
    return min;
  }
  return Math.min(Math.max(numeric, min), max);
}

export function expandColors(colors, length) {
  if (!colors || !colors.length) {
    return Array.from({ length }, () => 'rgba(37, 99, 235, 0.75)');
  }
  return Array.from({ length }, (_, index) => colors[index % colors.length]);
}
