import java.awt.geom.Ellipse2D;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class Light
{
	private Ellipse2D.Double extent;
	private double x, y, r;
	private Color color;
	private float brightness;
	
	/**
	 * <p>Instantiates a new light with extent of r radius</p>
	 * @param x The x location of the light to be centered to.
	 * @param y The y location of the light to be centered to.
	 * @param r The extent of the light to be centered for.
	 * @param color The color of the light
	 * @see java.awt.Color
	 */
	public Light(double x, double y, double r, Color color, float brightness)
	{
		this.extent = new Ellipse2D.Double( x-(r/2), y-(r/2), r/2, r/2 );
		
		this.x = x;
		this.y = y;
		this.r = r;
		this.color = color;
		this.brightness = brightness;
	}
	
	/**
	 * @return The current x location of the lights center
	 */
	public double getX() { return this.x; }
	/**
	 * @param x The new x location of the light to be centered to
	 */
	public void setX( double x ) { this.x = x; setExtent( this.x, this.y, this.r ); }
	
	/**
	 * @return The current y location of the lights center
	 */
	public double getY() { return this.y; }
	/**
	 * @param y The new y location of the light to be centered to
	 */
	public void setY( double y ) { this.y = y; setExtent( this.x, this.y, this.r ); }
	
	/**
	 * @return The current radius of the light
	 */
	public double getRadius() { return this.r; }
	/**
	 * @param r The new radius of the light to be centered from
	 */
	public void setRadius( double r ) { this.r = r; setExtent( this.x, this.y, this.r ); }
	
	/**
	 * @return The current color of the light
	 * @see java.awt.Color
	 */
	public Color getColor() { return this.color; }
	/**
	 * @param color The new color of the light
	 * @see java.awt.Color
	 */
	public void setColor( Color color ) { this.color = color; }
	
	public float getBrightness() { return this.brightness; }
	public void setBrightness( float brightness ) { this.brightness = brightness; }
	
	/**
	 * <p>This creates the new ellipsoid defining the light location and extent</p>
	 * @param x The x location of the light to be centered to.
	 * @param y The y location of the light to be centered to.
	 * @param r The radius of the light to be centered from.
	 */
	private void setExtent( double x, double y, double r) { this.extent = new Ellipse2D.Double( x-(r/2), y-(r/2), r/2, r/2 ); }

	/*
	 * <p>Updates light levels inside the range of lights passed in, does not affect the entire image nor does it retain the original image.</p>
	 * @param img A bufferedImage referring to the current scene
	 * @param lights An array of light objects referring to all the lights in the current scene or those that are being told to be drawn
	 * @return A new bufferedImage created from the original with the addition of lights and their respective color
	 */
	public static BufferedImage updateLightLevels(BufferedImage img, Light[] lights)
	{
		//Assign brightnessBuffer to the passed in image which should be the original scene
	    BufferedImage brightnessBuffer = img;
	    
	    //Assign the absence of color to the soon to be changed colors
	    Color rgbColor = Color.black;
	    Color lightCol = Color.black;
	    
	    //The pixels rgb integer value
	    int rgb = 0;
	    
	    //Distance variables
	    double oldDist = 0.0;
	    double newDist = 0.0;
	    
	    //Light values to be used for edition of pixels
	    float modifier = 1f;
	    float r, g, b;
	
		//loop through array of light objects
		for(Light l : lights)
		{
			/* Check for x locations inside the rectangle of the light
			*  This ensures a fast creation of lights that isnt CPU intensive, alternatively
			*  you could pass this off to the graphics card for number crunching through use of
			*  a library, the graphics card is a great number cruncher
			*/
			for( int x=(int)l.getX()-(int)l.getRadius(); x<(int)l.getX()+(int)l.getRadius(); x++ )
			{
				//Check for y locations inside the rectangle of the light
				for( int y=(int)l.getY()-(int)l.getRadius(); y<(int)l.getY()+(int)l.getRadius(); y++ )
				{
					//Check for x and y locations being inside the circle of the light
					if( Logic.getLightDistance((double)x, (double)y, l) <= l.getRadius() )
					{
						//Assign to the new distance from the lights center
						newDist = Logic.getLightDistance( (double)x, (double)y, l );

						//Get rgb from specific pixel
						rgb = brightnessBuffer.getRGB(x, y);
						
						if( y < l.getY() )
						{
							//Check for distance from center creating a cone
							if( newDist < oldDist)
								modifier += l.getBrightness();
							if( newDist >= oldDist)
								modifier = 1f;
						}
						else
						{
							//Check for distance from center creating a cone
							if( oldDist <= newDist )
								modifier -= l.getBrightness();
						}
							
						//make a new color
		                rgbColor = new Color(rgb);
		                
		                //Assign values to our pixels new rgb
		                r = rgbColor.getRed();
						g = rgbColor.getGreen();
						b = rgbColor.getBlue();
						
						//Get the lights color for addition later
						lightCol = l.getColor();
						
						//Add color to the light when created
						r += lightCol.getRed();
						g += lightCol.getGreen();
						b += lightCol.getBlue();
						
						//Multiply it by multiplier to effect brightness to create cone effect
						r *= modifier;
						g *= modifier;
						b *= modifier;
						
						//Check for values out of color space
						if( r > 255.0f ) r = 255.0f;
						if( r < 0.0f ) r = 0.0f;
						if( g > 255.0f ) g = 255.0f;
						if( g < 0.0f ) g = 0.0f;
						if( b > 255.0f ) b = 255.0f;
						if( b < 0.0f ) b = 0.0f;
						
						//Assign the new rgb values to a color for use in awt color space
						rgbColor = new Color(Math.round(r), Math.round(g), Math.round(b));
	
		                //set the pixel to the new color
		                brightnessBuffer.setRGB(x, y, rgbColor.getRGB());
		                
		                //Assign the old distance to the current location for checking later on
		                if( y <= l.getY() )
		                	oldDist = Logic.getLightDistance( (double)x, (double)y, l );
		                else
		                	newDist = Logic.getLightDistance( (double)x, (double)y, l );
					}
				}
			}
		}
	
	    return brightnessBuffer;
	}
	
	/**
	 * @return The light formatted to a String through a legacy method
	 * @see java.lang.String
	 */
	public String toString() { return "x: " + this.x + ", y: " + this.y + ", r: " + this.r + ", color: " + this.color; }
}
