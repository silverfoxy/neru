package androidbridge_test

import (
	"os"
	"path/filepath"
	"testing"

	"github.com/stretchr/testify/require"
	"github.com/y3owk1n/neru/internal/androidbridge"
)

func TestLoadConfigFileUsesProductionDefaultsAndOverrides(t *testing.T) {
	path := filepath.Join(t.TempDir(), "config.toml")
	require.NoError(t, os.WriteFile(path, []byte(`
[grid]
characters = "arstneio"
row_labels = "abc"
col_labels = "xyz"

[recursive_grid]
grid_cols = 4
grid_rows = 3
keys = "qwfparstzxcd"
max_depth = 7

[recursive_grid.hotkeys]
"Shift+Enter" = ["action left_click", "action left_click"]
`), 0o600))

	snapshot := androidbridge.LoadConfigFile(path)

	require.Empty(t, snapshot.Diagnostics)
	require.Equal(t, "arstneio", snapshot.Grid.Characters)
	require.Equal(t, "abc", snapshot.Grid.RowLabels)
	require.Equal(t, "xyz", snapshot.Grid.ColLabels)
	require.Equal(t, 4, snapshot.RecursiveGrid.Columns)
	require.Equal(t, 3, snapshot.RecursiveGrid.Rows)
	require.Equal(t, "qwfparstzxcd", snapshot.RecursiveGrid.Keys)
	require.Equal(t, 7, snapshot.RecursiveGrid.MaxDepth)
	require.Equal(t, []string{"action left_click", "action left_click"}, snapshot.RecursiveGrid.Hotkeys["Shift+Enter"])
}

func TestLoadConfigFileReturnsGoValidationDiagnostic(t *testing.T) {
	path := filepath.Join(t.TempDir(), "config.toml")
	require.NoError(t, os.WriteFile(path, []byte(`
[recursive_grid]
grid_cols = 4
grid_rows = 3
keys = "short"
`), 0o600))

	snapshot := androidbridge.LoadConfigFile(path)

	require.Len(t, snapshot.Diagnostics, 1)
	require.Empty(t, snapshot.RecursiveGrid.Keys)
}
