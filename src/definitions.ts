export interface ContactsPluginPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
