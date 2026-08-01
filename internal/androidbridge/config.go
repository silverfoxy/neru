// Package androidbridge defines the Go-owned policy surface that the Android
// JNI layer exposes to Kotlin. It deliberately contains no Android or JNI code
// so it can be exercised by normal Go tests.
package androidbridge

import (
	"github.com/y3owk1n/neru/internal/config"
	"go.uber.org/zap"
)

// Diagnostic is safe to serialize across the JNI boundary.
type Diagnostic struct {
	Message string `json:"message"`
}

// GridSnapshot is the Android-relevant portion of Neru's standard grid config.
type GridSnapshot struct {
	Characters   string              `json:"characters"`
	SublayerKeys string              `json:"sublayerKeys"`
	RowLabels    string              `json:"rowLabels"`
	ColLabels    string              `json:"colLabels"`
	Hotkeys      map[string][]string `json:"hotkeys"`
}

// RecursiveGridSnapshot is the Android-relevant recursive-grid policy.
type RecursiveGridSnapshot struct {
	Columns  int                 `json:"columns"`
	Rows     int                 `json:"rows"`
	Keys     string              `json:"keys"`
	MaxDepth int                 `json:"maxDepth"`
	Hotkeys  map[string][]string `json:"hotkeys"`
}

// ConfigSnapshot contains validated, default-resolved policy consumed by Android.
type ConfigSnapshot struct {
	Grid          GridSnapshot          `json:"grid"`
	RecursiveGrid RecursiveGridSnapshot `json:"recursiveGrid"`
	Diagnostics   []Diagnostic          `json:"diagnostics"`
}

// LoadConfigFile uses Neru's production Go parser, defaults, hotkey processing,
// and validators. JNI will pass the app-private config path to this function.
func LoadConfigFile(path string) ConfigSnapshot {
	service := config.NewService(config.DefaultConfig(), path, zap.NewNop(), nil)
	result := service.LoadWithValidation(path)
	if result.ValidationError != nil {
		return ConfigSnapshot{Diagnostics: []Diagnostic{{Message: result.ValidationError.Error()}}}
	}

	cfg := result.Config
	return ConfigSnapshot{
		Grid: GridSnapshot{
			Characters:   cfg.Grid.Characters,
			SublayerKeys: cfg.Grid.SublayerKeys,
			RowLabels:    cfg.Grid.RowLabels,
			ColLabels:    cfg.Grid.ColLabels,
			Hotkeys:      snapshotHotkeys(cfg.HotkeysForMode(config.ModeNameGrid)),
		},
		RecursiveGrid: RecursiveGridSnapshot{
			Columns:  cfg.RecursiveGrid.GridCols,
			Rows:     cfg.RecursiveGrid.GridRows,
			Keys:     cfg.RecursiveGrid.Keys,
			MaxDepth: cfg.RecursiveGrid.MaxDepth,
			Hotkeys:  snapshotHotkeys(cfg.HotkeysForMode(config.ModeNameRecursiveGrid)),
		},
	}
}

func snapshotHotkeys(source map[string]config.StringOrStringArray) map[string][]string {
	result := make(map[string][]string, len(source))
	for key, actions := range source {
		result[key] = append([]string(nil), actions...)
	}
	return result
}
