# Style Guide

## Language
- Java only. Never write Kotlin, Groovy, or any other JVM language for mod source code, this is to avoid dependency on FLK.
- Gradle build scripts stay Kotlin DSL (`*.gradle.kts`); that's a buildscript-tooling choice, not mod code.

## Lombok
- Use Lombok where it removes real boilerplate: `@Getter`/`@Setter`, `@RequiredArgsConstructor`/`@AllArgsConstructor`, `@Builder`, `@EqualsAndHashCode`.
- Never use Lombok on mixin classes.

## Access Widener
- Use `src/main/resources/downtime.accesswidener` to widen access to package-private/protected/final members needed by mixins or mod code, instead of reflection.
- Keep entries minimal and scoped to exactly what's needed.

## Structure
- `api` — public, reusable types other code depends on.
- `feature/impl` — concrete feature implementations; keep package-private where possible.
- `config` — configuration data and load/save logic.
- `command` — client commands.
- `compat` — integration/compatibility shims for other mods, currently only ModMenu.
- `data` — data types.
- `shader` — shader programs and render pipeline.
- `utils` — small stateless helpers.
- `mixin` — mixins; accessor/invoker mixins go in `mixin/accessor`, interface-injecting mixins go in `mixin/interfacemixins`, everything else in `mixin/mixins`.
- `extensions/<original package>/<ClassName>` — static extension classes for a type from another package, mirroring that type's package path (e.g. `extensions/net/minecraft/world/item/ItemStack/ItemStackExtensions.java`).
- Never put implementation details in `api`, and never put reusable/public types in `impl`.


