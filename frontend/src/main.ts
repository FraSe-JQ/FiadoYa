import { bootstrapApplication } from "@angular/platform-browser";
import { provideHttpClient, withXsrfConfiguration } from "@angular/common/http";
import { AppComponent } from "./app/app.component";
import { LOCALE_ID } from "@angular/core";
import { registerLocaleData } from "@angular/common";
import localeEsPe from "@angular/common/locales/es-PE";
registerLocaleData(localeEsPe);
bootstrapApplication(AppComponent, {
  providers: [
    { provide: LOCALE_ID, useValue: "es-PE" },
    provideHttpClient(
      withXsrfConfiguration({
        cookieName: "XSRF-TOKEN",
        headerName: "X-XSRF-TOKEN",
      }),
    ),
  ],
}).catch(console.error);
