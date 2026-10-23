document.addEventListener('DOMContentLoaded', function () {
  var btn = document.getElementById('sidebarToggle');
  if (btn) {
    btn.addEventListener('click', function () { document.body.classList.toggle('sidebar-open'); });
  }
  // close the mobile sidebar when the dimmed backdrop is tapped
  document.addEventListener('click', function (e) {
    if (document.body.classList.contains('sidebar-open') && !e.target.closest('.sidebar') && !e.target.closest('#sidebarToggle')) {
      document.body.classList.remove('sidebar-open');
    }
  });
  // confirm dialogs for destructive forms
  document.querySelectorAll('form[data-confirm]').forEach(function (f) {
    f.addEventListener('submit', function (e) { if (!window.confirm(f.dataset.confirm)) e.preventDefault(); });
  });
  // auto-dismiss flash messages
  setTimeout(function () {
    document.querySelectorAll('.alert-flash').forEach(function (a) { a.classList.remove('show'); a.remove(); });
  }, 6000);
});
