package androidbridge_test

import (
	"testing"

	"github.com/stretchr/testify/require"
	"github.com/y3owk1n/neru/internal/androidbridge"
)

func TestBuildGridSnapshotUsesFlexibleDesktopLabels(t *testing.T) {
	cells := androidbridge.BuildGridSnapshot(androidbridge.GridRequest{
		Characters: "qwfparstzxcd",
		RowLabels:  "qwf",
		ColLabels:  "arst",
		Bounds:     androidbridge.Rect{Left: 0, Top: 0, Right: 1080, Bottom: 2400},
	})

	require.NotEmpty(t, cells)
	require.Contains(t, cells[0].Coordinate, "Q")
	for _, cell := range cells {
		require.Greater(t, cell.Bounds.Right, cell.Bounds.Left)
		require.Greater(t, cell.Bounds.Bottom, cell.Bounds.Top)
		require.GreaterOrEqual(t, cell.CenterX, cell.Bounds.Left)
		require.GreaterOrEqual(t, cell.CenterY, cell.Bounds.Top)
	}
}
