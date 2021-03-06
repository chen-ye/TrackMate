package fiji.plugin.trackmate.visualization.trackscheme;

import static fiji.plugin.trackmate.gui.TrackMateWizard.FONT;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxGraphHandler;
import com.mxgraph.swing.view.mxCellEditor;
import com.mxgraph.swing.view.mxICellEditor;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;

import fiji.plugin.trackmate.Model;

public class TrackSchemeGraphComponent extends mxGraphComponent implements mxIEventListener
{

	private static final long serialVersionUID = -1L;

	private static final Color BACKGROUND_COLOR_1 = Color.GRAY;

	private static final Color BACKGROUND_COLOR_2 = Color.LIGHT_GRAY;

	private static final Color LINE_COLOR = Color.BLACK;

	private static final int MAX_DECCORATION_LEVELS = 2;

	/** The width of the columns for each track, from left to right. */
	int[] columnWidths = new int[ 0 ];

	/** The trackID for each column. */
	Integer[] columnTrackIDs;

	private final TrackScheme trackScheme;

	private int paintDecorationLevel = TrackScheme.DEFAULT_PAINT_DECORATION_LEVEL;

	/*
	 * CONSTRUCTOR
	 */

	public TrackSchemeGraphComponent( final JGraphXAdapter graph, final TrackScheme trackScheme )
	{
		super( graph );
		this.trackScheme = trackScheme;

		getViewport().setOpaque( true );
		getViewport().setBackground( BACKGROUND_COLOR_1 );
		setZoomFactor( 2.0 );

		connectionHandler.addListener( mxEvent.CONNECT, this );

		// Our own cell painter, that displays an image (if any) and a label
		// next to it.
		mxGraphics2DCanvas.putShape( mxScaledLabelShape.SHAPE_NAME, new mxScaledLabelShape() );
		// Replace default painter for edge label so that we can draw labels
		// parallel to edges.
		mxGraphics2DCanvas.putTextShape( mxGraphics2DCanvas.TEXT_SHAPE_DEFAULT, new mxSideTextShape() );

	}

	/*
	 * METHODS
	 */

	@Override
	public JGraphXAdapter getGraph()
	{
		return ( JGraphXAdapter ) super.getGraph();
	}

	@Override
	public boolean isToggleEvent( final MouseEvent event )
	{
		return event.isShiftDown();
	}

	/**
	 * Overridden to customize the look of the editor. We want to hide the image
	 * in the background.
	 */
	@Override
	protected mxICellEditor createCellEditor()
	{
		final mxCellEditor editor = new mxCellEditor( this )
		{
			@Override
			public void startEditing( final Object cell, final EventObject evt )
			{
				textArea.setOpaque( true );
				textArea.setBorder( BorderFactory.createLineBorder( Color.ORANGE, 1 ) );
				super.startEditing( cell, evt );
			}
		};
		editor.setShiftEnterSubmitsText( true );
		return editor;
	}

	/**
	 * Custom {@link mxGraphHandler} so as to avoid clearing the selection when
	 * right-clicking elsewhere than on a cell, which is reserved for aimed at
	 * displaying a popup menu.
	 */
	@Override
	protected mxGraphHandler createGraphHandler()
	{
		return new mxGraphHandler( this )
		{

			@Override
			public void mousePressed( final MouseEvent e )
			{
				if ( graphComponent.isEnabled() && isEnabled() && !e.isConsumed() && !graphComponent.isForceMarqueeEvent( e ) )
				{
					cell = graphComponent.getCellAt( e.getX(), e.getY(), false );
					initialCell = cell;

					if ( cell != null )
					{
						if ( isSelectEnabled() && !graphComponent.getGraph().isCellSelected( cell ) )
						{
							graphComponent.selectCellForEvent( cell, e );
							cell = null;
						}

						// Starts move if the cell under the mouse is movable
						// and/or any
						// cells of the selection are movable
						if ( isMoveEnabled() && !e.isPopupTrigger() )
						{
							start( e );
							e.consume();
						}
					}
				}
			}

			@Override
			public void mouseReleased( final MouseEvent e )
			{
				if ( graphComponent.isEnabled() && isEnabled() && !e.isConsumed() )
				{
					final mxGraph graph = graphComponent.getGraph();
					double dx = 0;
					double dy = 0;

					if ( first != null && ( cellBounds != null || movePreview.isActive() ) )
					{
						final double scale = graph.getView().getScale();
						final mxPoint trans = graph.getView().getTranslate();

						// TODO: Simplify math below, this was copy pasted from
						// getPreviewLocation with the rounding removed
						dx = e.getX() - first.x;
						dy = e.getY() - first.y;

						if ( cellBounds != null )
						{
							final double dxg = ( ( cellBounds.getX() + dx ) / scale ) - trans.getX();
							final double dyg = ( ( cellBounds.getY() + dy ) / scale ) - trans.getY();

							final double x = ( ( dxg + trans.getX() ) * scale ) + ( bbox.getX() ) - ( cellBounds.getX() );
							final double y = ( ( dyg + trans.getY() ) * scale ) + ( bbox.getY() ) - ( cellBounds.getY() );

							dx = Math.round( ( x - bbox.getX() ) / scale );
							dy = Math.round( ( y - bbox.getY() ) / scale );
						}
					}

					if ( first == null || !graphComponent.isSignificant( e.getX() - first.x, e.getY() - first.y ) )
					{
						// Delayed handling of selection
						if ( cell != null && !e.isPopupTrigger() && isSelectEnabled() && ( first != null || !isMoveEnabled() ) )
						{
							graphComponent.selectCellForEvent( cell, e );
						}

						// Delayed folding for cell that was initially under the
						// mouse
						if ( graphComponent.isFoldingEnabled() && graphComponent.hitFoldingIcon( initialCell, e.getX(), e.getY() ) )
						{
							fold( initialCell );
						}
						else
						{
							// Handles selection if no cell was initially under
							// the mouse
							final Object tmp = graphComponent.getCellAt( e.getX(), e.getY(), graphComponent.isSwimlaneSelectionEnabled() );

							if ( cell == null && first == null )
							{
								if ( tmp == null && e.getButton() == MouseEvent.BUTTON1 )
								{
									graph.clearSelection(); // JYT I did this to
															// keep selection
															// even if we
															// right-click
															// elsewhere
								}
								else if ( graph.isSwimlane( tmp ) && graphComponent.getCanvas().hitSwimlaneContent( graphComponent, graph.getView().getState( tmp ), e.getX(), e.getY() ) )
								{
									graphComponent.selectCellForEvent( tmp, e );
								}
							}

							if ( graphComponent.isFoldingEnabled() && graphComponent.hitFoldingIcon( tmp, e.getX(), e.getY() ) )
							{
								fold( tmp );
								e.consume();
							}
						}
					}
					else if ( movePreview.isActive() )
					{
						if ( graphComponent.isConstrainedEvent( e ) )
						{
							if ( Math.abs( dx ) > Math.abs( dy ) )
							{
								dy = 0;
							}
							else
							{
								dx = 0;
							}
						}

						final mxCellState markedState = marker.getMarkedState();
						Object target = ( markedState != null ) ? markedState.getCell() : null;

						// FIXME: Cell is null if selection was carried out,
						// need other variable
						// trace("cell", cell);

						if ( target == null && isRemoveCellsFromParent() && shouldRemoveCellFromParent( graph.getModel().getParent( initialCell ), cells, e ) )
						{
							target = graph.getDefaultParent();
						}

						final boolean clone = isCloneEnabled() && graphComponent.isCloneEvent( e );
						final Object[] result = movePreview.stop( true, e, dx, dy, clone, target );

						if ( cells != result )
						{
							graph.setSelectionCells( result );
						}

						e.consume();
					}
					else if ( isVisible() )
					{
						if ( constrainedEvent )
						{
							if ( Math.abs( dx ) > Math.abs( dy ) )
							{
								dy = 0;
							}
							else
							{
								dx = 0;
							}
						}

						final mxCellState targetState = marker.getValidState();
						final Object target = ( targetState != null ) ? targetState.getCell() : null;

						if ( graph.isSplitEnabled() && graph.isSplitTarget( target, cells ) )
						{
							graph.splitEdge( target, cells, dx, dy );
						}
						else
						{
							moveCells( cells, dx, dy, target, e );
						}

						e.consume();
					}
				}

				reset();
			}

			@Override
			public void mouseClicked( final MouseEvent event )
			{
				if ( !( event.getClickCount() == 2 ) ) { return; }

				final int x = event.getPoint().x;
				final int y = event.getPoint().y;
				final float scale = ( float ) graph.getView().getScale();

				// Scaled sizes
				final int xcs = Math.round( TrackScheme.X_COLUMN_SIZE * scale );
				final int ycs = Math.round( TrackScheme.Y_COLUMN_SIZE * scale );

				if ( y > ycs || x < xcs ) { return; }

				// Look for target column
				if ( null != columnWidths )
				{
					int column;
					int xc = xcs;
					for ( column = 0; column < columnWidths.length; column++ )
					{
						final int cw = columnWidths[ column ];
						xc += cw * xcs;
						if ( x > xcs && x < xc )
						{
							break;
						}
					}

					if ( column >= columnWidths.length ) { return; }

					final String oldName = trackScheme.getModel().getTrackModel().name( columnTrackIDs[ column ] );
					final Integer trackID = columnTrackIDs[ column ];

					final JScrollPane scrollPane = new JScrollPane();
					scrollPane.getViewport().setOpaque( false );
					scrollPane.setVisible( false );
					scrollPane.setOpaque( false );
					scrollPane.setBounds( ( xc - ( columnWidths[ column ] ) * xcs ), 0, columnWidths[ column ] * xcs, ycs );
					scrollPane.setVisible( true );

					// Creates the plain text editor
					final JTextField textArea = new JTextField( oldName );
					textArea.setBorder( BorderFactory.createLineBorder( Color.ORANGE, 2 ) );
					textArea.setOpaque( true );
					textArea.setBackground( BACKGROUND_COLOR_1 );
					textArea.setHorizontalAlignment( JTextField.CENTER );
					textArea.setFont( FONT.deriveFont( 12 * scale ).deriveFont( Font.BOLD ) );
					textArea.addActionListener( new ActionListener()
					{
						// Get track name and pass it tp model
						@Override
						public void actionPerformed( final ActionEvent arg0 )
						{
							final String newname = textArea.getText();
							trackScheme.getModel().getTrackModel().setName( trackID, newname );
							scrollPane.remove( textArea );
							getGraphControl().remove( scrollPane );
							TrackSchemeGraphComponent.this.repaint();
						}
					} );

					scrollPane.setViewportView( textArea );
					getGraphControl().add( scrollPane, 0 );

					textArea.revalidate();
					textArea.requestFocusInWindow();
					textArea.selectAll();

				}

			}

		};
	}

	/**
	 * Override this so as to paint the background with colored rows and
	 * columns.
	 */
	@Override
	public void paintBackground( final Graphics g )
	{
		if ( paintDecorationLevel == 0 )
			return;

		final int width = getViewport().getView().getSize().width;
		final int height = getViewport().getView().getSize().height;
		final double scale = graph.getView().getScale();

		final Graphics2D g2d = ( Graphics2D ) g;
		g.setFont( FONT.deriveFont( ( float ) ( 12 * scale ) ).deriveFont( Font.BOLD ) );
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		final Rectangle paintBounds = g.getClipBounds();


		// Scaled sizes
		final double xcs = TrackScheme.X_COLUMN_SIZE * scale;
		final double ycs = TrackScheme.Y_COLUMN_SIZE * scale;

		if ( paintDecorationLevel > 1 )
		{
			// Alternating row color
			g.setColor( BACKGROUND_COLOR_2 );
			double y = 0;
			while ( y < height )
			{
				if ( y > paintBounds.y - ycs && y < paintBounds.y + paintBounds.height )
					g.fillRect( 0, ( int ) y, width, ( int ) ycs );
				y += 2d * ycs;
			}

			// Header separator
			g.setColor( LINE_COLOR );
			if ( ycs > paintBounds.y && ycs < paintBounds.y + paintBounds.height )
				g.drawLine( paintBounds.x, ( int ) ycs, paintBounds.x + paintBounds.width, ( int ) ycs );
			if ( xcs > paintBounds.x && xcs < paintBounds.x + paintBounds.width )
				g.drawLine( ( int ) xcs, paintBounds.y, ( int ) xcs, paintBounds.y + paintBounds.height );

			// Row headers
			final double x = xcs / 4d;
			y = 3 * ycs / 2d;

			if ( xcs > paintBounds.x )
			{
				while ( y < height )
				{
					if ( y > paintBounds.y - ycs && y < paintBounds.y + paintBounds.height )
					{
						final int frame = ( int ) ( y / ycs - 1 );
						// g.drawString(String.format("%.1f " + timeUnits, frame
						// *
						// dt), x, y);
						g.drawString( String.format( "frame %d", frame ), ( int ) x, ( int ) Math.round( y + 12 * scale ) );
					}
					y += ycs;
				}
			}

		}

		if ( paintDecorationLevel > 0 )
		{
			double x = xcs;
			// Column headers
			if ( null != columnWidths )
			{
				for ( int i = 0; i < columnWidths.length; i++ )
				{
					final int cw = columnWidths[ i ];
					String columnName = trackScheme.getModel().getTrackModel().name( columnTrackIDs[ i ] );
					if ( null == columnName )
					{
						columnName = "Name not set";
					}
					g.drawString( columnName, ( int ) ( x + 20d ), ( int ) ( ycs / 2d ) );
					g.setColor( LINE_COLOR );
					x += cw * xcs;
					g.drawLine( ( int ) x, 0, ( int ) x, height );
				}
			}
			// Last column header
			g.setColor( Color.decode( TrackScheme.DEFAULT_COLOR ) );
			g.drawString( "Unlaid spots", ( int ) ( x + 20d ), ( int ) ( ycs / 2d ) );
		}

	}

	/**
	 * This listener method will be invoked when a new edge has been created
	 * interactively in the graph component. It is used then to update the
	 * underlying {@link Model}.
	 */
	@Override
	public void invoke( final Object sender, final mxEventObject evt )
	{
		final Map< String, Object > props = evt.getProperties();
		final Object obj = props.get( "cell" );
		final mxCell cell = ( mxCell ) obj;
		trackScheme.addEdgeManually( cell );
		evt.consume();
	}

	@Override
	public void zoomTo( final double newScale, final boolean center )
	{
		final mxGraphView view = graph.getView();
		final double scale = view.getScale();

		final mxPoint translate = ( pageVisible && centerPage ) ? getPageTranslate( newScale ) : new mxPoint();
		graph.getView().scaleAndTranslate( newScale, translate.getX(), translate.getY() );

		if ( keepSelectionVisibleOnZoom && !graph.isSelectionEmpty() )
		{
			getGraphControl().scrollRectToVisible( view.getBoundingBox( graph.getSelectionCells() ).getRectangle() );
		}
		else
		{
			maintainScrollBar( true, newScale / scale, center );
			maintainScrollBar( false, newScale / scale, center );
		}
	}

	public void loopPaintDecorationLevel()
	{
		if ( paintDecorationLevel++ >= MAX_DECCORATION_LEVELS )
		{
			paintDecorationLevel = 0;
		}
		repaint();
	}

}
