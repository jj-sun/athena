declare module 'vue' {
  interface ComponentCustomProperties {
    $urlPath: Record<string, string>;
  }
}