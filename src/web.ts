import { WebPlugin } from '@capacitor/core';

import type { ContactsPluginPlugin } from './definitions';

export class ContactsPluginWeb extends WebPlugin implements ContactsPluginPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
