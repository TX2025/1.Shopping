<template>
  <router-view />
</template>

<style>
:root {
  --color-primary: #00676b;
  --color-primary-light: #0a8f94;
  --color-danger: #e74c3c;
  --color-text: #333333;
  --color-text-secondary: #666666;
  --color-text-muted: #999999;
  --color-bg: #f5f5f5;
  --color-white: #ffffff;
  --color-border: #e8e8e8;
  --shadow-sm: 0 2px 8px rgba(0,0,0,0.06);
  --shadow-md: 0 4px 16px rgba(0,0,0,0.08);
  --shadow-lg: 0 8px 24px rgba(0,0,0,0.1);
  --radius: 8px;
  --transition: all 0.3s ease;
}
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: 'Microsoft YaHei', 'PingFang SC', 'Helvetica Neue', Arial, sans-serif; color: var(--color-text); background: var(--color-bg); }
a { text-decoration: none; color: inherit; }
img { max-width: 100%; height: auto; }
</style>
