<template>
  <router-view />
</template>

<style>
:root {
  --color-primary: #00676b;
  --color-primary-light: #0a8f94;
  --color-primary-dark: #005055;
  --color-danger: #e74c3c;
  --color-danger-light: #ff6b6b;
  --color-text: #333333;
  --color-text-secondary: #666666;
  --color-text-muted: #999999;
  --color-bg: #f5f5f5;
  --color-white: #ffffff;
  --color-border: #e8e8e8;
  --color-border-light: #f0f0f0;
  --shadow-sm: 0 2px 8px rgba(0,0,0,0.06);
  --shadow-md: 0 4px 16px rgba(0,0,0,0.08);
  --shadow-lg: 0 8px 32px rgba(0,0,0,0.12);
  --shadow-xl: 0 16px 48px rgba(0,0,0,0.16);
  --radius: 8px;
  --radius-sm: 4px;
  --radius-lg: 12px;
  --transition: all 0.3s ease;
  --font-family: 'Microsoft YaHei', 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
}
* { margin: 0; padding: 0; box-sizing: border-box; }
body {
  font-family: var(--font-family);
  color: var(--color-text);
  background: var(--color-bg);
  -webkit-font-smoothing: antialiased;
}
a { text-decoration: none; color: inherit; }
img { max-width: 100%; height: auto; }

/* Global Element Plus overrides */
.el-button--primary {
  --el-button-bg-color: var(--color-primary);
  --el-button-border-color: var(--color-primary);
  --el-button-hover-bg-color: var(--color-primary-light);
  --el-button-hover-border-color: var(--color-primary-light);
  --el-button-active-bg-color: var(--color-primary-dark);
}
.el-button--danger {
  --el-button-hover-bg-color: var(--color-danger-light);
}
.el-pagination.is-background .el-pager li.is-active {
  background-color: var(--color-primary);
}
.el-menu-item.is-active {
  color: var(--color-primary);
  border-bottom-color: var(--color-primary);
}

/* Global section title style */
.section-title {
  text-align: center;
  font-size: 26px;
  color: #333;
  margin: 20px 0 28px;
  position: relative;
  font-weight: 600;
}
.section-title::after {
  content: '';
  display: block;
  width: 50px;
  height: 3px;
  background: var(--color-primary);
  margin: 12px auto 0;
  border-radius: 2px;
}
</style>
