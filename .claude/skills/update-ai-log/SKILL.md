---
name: update-ai-log
description: Añade una entrada al AI_LOG.md del proyecto al terminar cada petición hecha con IA (fecha y hora, prompt resumido, qué se hizo, decisiones y correcciones humanas). Usar SIEMPRE al cerrar una tarea.
---

# update-ai-log

`AI_LOG.md` es el entregable de la **Parte 4** de la prueba ("qué herramientas, en qué partes, qué decisiones/correcciones"). Debe poder leerse sin contexto.

## Cuándo
Al final de **cada** petición que haya cambiado algo en este repositorio (código, docs, config). Una entrada por petición. Si la petición tocó front y back, se añade una entrada en el `AI_LOG.md` de **cada** repo, solo con lo que afecte a ese repo.

## Cómo
1. Obtener la hora real: `date "+%Y-%m-%d %H:%M"` (no inventarla).
2. Añadir la entrada **al final** de la sección "Registro", con el siguiente número correlativo.
3. Si la entrada aporta algo nuevo a las tablas resumen del principio (nueva herramienta, nueva corrección relevante), actualizarlas también.

## Plantilla
```markdown
### #NN · YYYY-MM-DD HH:MM · <título corto en imperativo>
- **Herramienta:** Claude Code (<modelo>)
- **Prompt (resumen):** <qué pidió el usuario, en una o dos frases, en sus términos>
- **Qué hizo la IA:** <acciones concretas; archivos o carpetas afectados>
- **Decisiones / correcciones humanas:** <qué cambió o rechazó el usuario, y por qué> (o "Ninguna")
- **Resultado:** <estado final: hecho, pendiente, verificado o no>
```

## Reglas
- Español, frases claras, sin relleno.
- Nunca copiar secretos, tokens ni datos personales.
- No reescribir entradas antiguas salvo para corregir un error de hecho (y entonces indicarlo).
