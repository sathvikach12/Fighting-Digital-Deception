(() => {
  const rows = [...document.querySelectorAll('#categoryRows > div')];
  const categoryLabels = rows.map(row => row.children[0].textContent.trim());
  const categoryValues = rows.map(row => Number(row.children[1].textContent.trim()));
  const popular = [...document.querySelectorAll('#popularData span')];
  const palette = ['#4f8cff', '#3fd0b0', '#a98bff', '#ffb454', '#ff6f91', '#4cc9f0'];
  const base = {responsive: true, plugins: {legend: {display: false}}};
  const categoryCanvas = document.getElementById('categoryChart');
  if (categoryCanvas && window.Chart) new Chart(categoryCanvas, {type: 'bar', data: {labels: categoryLabels, datasets: [{data: categoryValues, backgroundColor: palette, borderRadius: 7}]}, options: {...base, scales: {x: {ticks: {color: '#b9c7da'}, grid: {display: false}}, y: {beginAtZero: true, ticks: {precision: 0, color: '#b9c7da'}, grid: {color: '#243955'}}}}});
  const popularCanvas = document.getElementById('popularChart');
  if (popularCanvas && window.Chart) new Chart(popularCanvas, {type: 'bar', data: {labels: popular.map(item => item.dataset.title), datasets: [{data: popular.map(item => Number(item.dataset.views)), backgroundColor: '#3fd0b0', borderRadius: 7}]}, options: {...base, indexAxis: 'y', scales: {x: {beginAtZero: true, ticks: {color: '#b9c7da'}, grid: {color: '#243955'}}, y: {ticks: {color: '#b9c7da'}, grid: {display: false}}}}});
})();
