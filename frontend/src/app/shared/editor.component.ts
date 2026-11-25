import { Component, input, output, effect } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { FieldComponent } from "./field.component";
export interface EditorField {
  key: string;
  label: string;
  help: string;
  type?: string;
  required?: boolean;
  options?: { value: string | number; label: string }[];
  min?: number;
  max?: number;
  step?: string;
  section?: string;
}
@Component({
  selector: "fy-editor",
  standalone: true,
  imports: [FormsModule, FieldComponent],
  template: ` <form (ngSubmit)="submit()" class="editor">
    <div class="form-grid">
      @for (field of fields(); track field.key) {
        @if (field.section) {
          <h3 class="form-section">{{ field.section }}</h3>
        }
        <fy-field [label]="field.label" [help]="field.help">
          <label class="sr-only" [for]="'edit-' + field.key">{{
            field.label
          }}</label>
          @if (field.options) {
            <select
              [id]="'edit-' + field.key"
              [name]="field.key"
              [(ngModel)]="values[field.key]"
              [required]="field.required ?? true"
            >
              @for (option of field.options; track option.value) {
                <option [ngValue]="option.value">{{ option.label }}</option>
              }
            </select>
          } @else if (field.type === "checkbox") {
            <input
              [id]="'edit-' + field.key"
              [name]="field.key"
              type="checkbox"
              [(ngModel)]="values[field.key]"
            />
          } @else if (field.type === "textarea") {
            <textarea
              [id]="'edit-' + field.key"
              [name]="field.key"
              [(ngModel)]="values[field.key]"
              [required]="field.required ?? true"
              maxlength="4000"
            ></textarea>
          } @else {
            <input
              [id]="'edit-' + field.key"
              [name]="field.key"
              [type]="field.type ?? 'text'"
              [(ngModel)]="values[field.key]"
              [required]="field.required ?? true"
              [min]="field.min ?? 0"
              [max]="field.max ?? 999999999"
              [step]="field.step ?? 'any'"
              [attr.maxlength]="field.type === 'password' ? 72 : 250"
            />
          }
        </fy-field>
      }
    </div>
    <div class="form-actions">
      <button type="button" class="secondary" (click)="cancel.emit()">
        Cancelar</button
      ><button type="submit" [disabled]="busy()">
        {{ busy() ? "Guardando…" : submitLabel() }}
      </button>
    </div>
  </form>`,
})
export class EditorComponent {
  fields = input.required<EditorField[]>();
  initial = input<Record<string, any>>({});
  busy = input(false);
  submitLabel = input("Guardar");
  saved = output<Record<string, any>>();
  cancel = output();
  values: Record<string, any> = {};
  constructor() {
    effect(() => (this.values = { ...this.initial() }));
  }
  submit() {
    this.saved.emit({ ...this.values });
  }
}
