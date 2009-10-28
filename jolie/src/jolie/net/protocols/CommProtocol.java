/***************************************************************************
 *   Copyright (C) by Fabrizio Montesi                                     *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Library General Public License as       *
 *   published by the Free Software Foundation; either version 2 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU Library General Public     *
 *   License along with this program; if not, write to the                 *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 *                                                                         *
 *   For details about the authors of this software, see the AUTHORS file. *
 ***************************************************************************/


package jolie.net.protocols;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jolie.net.CommChannel;
import jolie.net.CommMessage;
import jolie.runtime.Value;
import jolie.runtime.ValueVector;
import jolie.runtime.VariablePath;

/**
 * A CommProtocol implements a protocol for sending and receiving data under the form of CommMessage objects.
 * This class should not be extended directly; see {@link ConcurrentCommProtocol ConcurrentCommProtocol} and {@link SequentialCommProtocol SequentialCommProtocol} instead.
 * @author Fabrizio Montesi
 */
public abstract class CommProtocol
{
	private final static class LazyDummyChannelHolder {
		private LazyDummyChannelHolder() {}
		private static class DummyChannel extends CommChannel {
			public void closeImpl() {}
			public void sendImpl( CommMessage message ) {}
			public CommMessage recvImpl() { return CommMessage.UNDEFINED_MESSAGE; }
		}

		private static DummyChannel dummyChannel = new DummyChannel();
	}


	private final VariablePath configurationPath;
	private CommChannel channel = null;

	protected VariablePath configurationPath()
	{
		return configurationPath;
	}

	public abstract String name();
	
	public CommProtocol( VariablePath configurationPath )
	{
		this.configurationPath = configurationPath;
	}
	
	public void setChannel( CommChannel channel )
	{
		this.channel = channel;
	}

	protected CommChannel channel()
	{
		if ( this.channel == null ) {
			return LazyDummyChannelHolder.dummyChannel;
		}
		return this.channel;
	}
	
	protected ValueVector getParameterVector( String id )
	{
		return configurationPath.getValue().getChildren( id );
	}
	
	protected boolean hasParameter( String id )
	{
		if ( configurationPath.getValue().hasChildren( id ) ) {
			return configurationPath.getValue().getFirstChild( id ).isDefined();
		}
		return false;
	}
	
	/**
	 * Shortcut for getParameterVector( id ).first()
	 */
	protected Value getParameterFirstValue( String id )
	{
		return getParameterVector( id ).first();
	}
	
	/**
	 * Shortcut for checking if a parameter intValue() equals 1
	 * @param id the parameter identifier
	 */
	protected boolean checkBooleanParameter( String id )
	{
		return hasParameter( id ) && getParameterFirstValue( id ).intValue() == 1;
	}

	/**
	 * Shortcut for checking if a parameter intValue() equals 1
	 * @param id the parameter identifier
	 */
	protected boolean checkBooleanParameter( String id, boolean defaultValue )
	{
		if ( hasParameter( id ) ) {
			return getParameterFirstValue( id ).intValue() == 1;
		} else {
			return defaultValue;
		}
	}
	
	/**
	 * Shortcut for <code>getParameterFirstValue( id ).strValue()</code>
	 * @param id the parameter identifier
	 */
	protected String getStringParameter( String id )
	{
		return ( hasParameter( id ) ? getParameterFirstValue( id ).strValue() : "" );
	}

	/**
	 * Shortcut for <code>getParameterFirstValue( id ).intValue()</code>
	 * @param id the parameter identifier
	 */
	protected int getIntParameter( String id )
	{
		return ( hasParameter( id ) ? getParameterFirstValue( id ).intValue() : 0 );
	}
	
	abstract public CommMessage recv( InputStream istream, OutputStream ostream )
		throws IOException;

	abstract public void send( OutputStream ostream, CommMessage message, InputStream istream )
		throws IOException;

	abstract public boolean isThreadSafe();
}
