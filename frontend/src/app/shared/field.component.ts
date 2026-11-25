import { Component, input } from "@angular/core";
@Component({
  selector: "fy-field",
  standalone: true,
  template: `<div class="field">
    <div class="field-title">
      <span>{{ label() }}</span>
      <details>
        <summary [attr.aria-label]="'Ayuda: ' + label()">?</summary>
        <p>{{ help() }}</p>
      </details>
    </div>
    <ng-content />
  </div>`,
})
export class FieldComponent {
  label = input.required<string>();
  help = input.required<string>();
}
