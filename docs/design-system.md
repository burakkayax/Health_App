# Health App Design System

This document outlines the core principles, tokens, and components for the Health App.

## Design Principles
1. **Premium & Calm**: The UI uses glassmorphic surfaces, subtle shadows, and pastel accents to feel calming.
2. **Accessible**: High contrast on text, large tap targets (minimum 48dp).
3. **Consistent**: Uniform border radii, consistent padding, and standard motion curves.

## Tokens

### Colors
- `SystemBlue` (Primary action)
- `GlassSurface` & `GlassBorder` (Cards)
- `Ink`, `SecondaryText`, `TertiaryText` (Typography)

### Spacing & Radius
- Use `HealthSpacing.cardPadding` inside cards.
- Use `HealthSpacing.cardVerticalSpacing` between sections.
- Use `HealthShapeTokens.cardRadius` (32dp) for cards, `sheetRadius` for bottom sheets.

### Typography
- Titles: `HealthTypography.titleMedium`, `titleLarge`
- Values: `HealthTypography.displayMedium`, `displayLarge`

## Components

### Cards
- `HealthSummaryMetricCard`: Used on the dashboard for high-level metrics.
- `HealthDetailHeroCard`: Used at the top of detail screens (Weight, Sleep).
- `HealthTrendCard`: Wrapper for charts.
- `HealthHistoryCard`: Wrapper for lists of past entries.
- `HealthAddEntryCard`: Form container.

### Forms & Inputs
- `HealthTextInput`, `HealthNumberInput`: Pill-shaped, 58dp height.
- `HealthPrimaryButton`: Primary actions, pill-shaped.
- `HealthSelectionChip`: For discrete choices.

### State
- `HealthEmptyState`: Shown when no data exists.
- `HealthLoadingState`: Centered progress.
- `HealthErrorState`: For failures.

### Motion
Use `HealthMotion` functions:
- `standardTween()`: general layout changes.
- `sheetEnter()`, `sheetExit()`: pickers.
- `cardPress()`: touch feedback.
