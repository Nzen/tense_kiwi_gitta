/* see ../../../../../LICENSE for release details */
package ws.nzen.format.jq.tkg;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.*;

import org.apache.commons.lang3.tuple.Pair;

import org.jooq.codegen.JavaGenerator;
import org.jooq.codegen.JavaWriter;
import org.jooq.meta.ColumnDefinition;
import org.jooq.meta.DataTypeDefinition;
import org.jooq.meta.TableDefinition;

/**

*/
public class AppendsRepositoryToPojo extends JavaGenerator
{

	/**  */
	public AppendsRepositoryToPojo()
	{
		super();
	}


	// unready
	protected void generatePojoClassFooter(
			TableDefinition table, JavaWriter out
	) {
		out.println( "\t// table name is "+ table.getName() );

		Collection<ColumnDef> primaryKeys = new LinkedList<>();
		Collection<ColumnDef> otherColumns = new ArrayList<>();

		for ( ColumnDefinition column : table.getPrimaryKey().getKeyColumns() )
		{
			primaryKeys.add( asTkgColumn( column ) );
		}
		for ( ColumnDefinition column : table.getColumns() )
		{
			otherColumns.add( asTkgColumn( column ) );
		}
		otherColumns.removeAll( primaryKeys );

		// 4tests
		for ( ColumnDef def : primaryKeys )
		{
			out.println( "\t// column "+ def );
		}
		for ( ColumnDef def : otherColumns )
		{
			out.println( "\t// column "+ def );
		}
		// ¶ the interface has one id slot, so this combines them as Pair<?,?> or throws when compound id
		ColumnDef fullPrimaryKey = combineIfMultiplePrimaryKeyColumns( (List<ColumnDef>)primaryKeys );

		addDeleteById( table, fullPrimaryKey, out );
		// the rest of the spec
	}


	// maybe okay
	private ColumnDef combineIfMultiplePrimaryKeyColumns(
			List<ColumnDef> primaryKeys
	) {
		if ( primaryKeys.isEmpty() )
			throw new RuntimeException( "no pk, eh?" );
		if ( primaryKeys.size() == 1 )
			return primaryKeys.get( 0 );
		else if ( primaryKeys.size() == 2 )
		{
			ColumnDef comboPkViaPair = new ColumnDef();
			comboPkViaPair.jtype = ParameterizedTypeName.get(
					ClassName.get( Pair.class ),
					primaryKeys.get( 0 ).jtype,
					primaryKeys.get( 1 ).jtype );
			comboPkViaPair.jooqName = "compoundId"; // FIX I guess this is a bad idea
			return comboPkViaPair;
		}
		else
			throw new RuntimeException( "pk of more than three columns is unimplemented" );
	}


	// maybe okay
	private ColumnDef asTkgColumn(
			ColumnDefinition jooqStyle
	) {
		ColumnDef tkgStyle = new ColumnDef();
		tkgStyle.jtype = tnFromJooq( jooqStyle.getType() );
		tkgStyle.jooqName = jooqStyle.getName();
		tkgStyle.sqlTypeNonenum = sqlTypeIntFromJooq( jooqStyle.getType() );
		return tkgStyle;
	}


	// unready
	/**
	<code>
		public void deleteById(
			Integer id
	) {
		if ( id == null )
			return;
		Object[] value = { id };
		int[] type = { Types.INTEGER };
		String delete = sql.deleteFrom( ECI_RETRIEVAL_MECHANISM )
				.where( ECI_RETRIEVAL_MECHANISM.RETRIEVAL_MECHANISM_ID.eq( id ) )
				.getSQL();
		jdbc.update( delete, value, type );
	}
	</code>
	*/
	private void addDeleteById(
			TableDefinition table, ColumnDef primaryKey, JavaWriter out
	) {
		String paramName = "id";
		String valueArrayName = "value", typeArrayName = "type";
		String deleteQueryName = "delete",
				jooqName = "sql",
				jdbcTemplateName = "jdbc";
		MethodSpec deleteById = MethodSpec.methodBuilder( "deleteById" )
				.addModifiers( Modifier.PUBLIC )
				.returns( void.class )
				.addParameter( primaryKey.jtype, paramName )
				.beginControlFlow( "$L == null", paramName )
					.addStatement( "return" )
				.endControlFlow()
				.addStatement( "$T $L = { $L }",
						ArrayTypeName.of( ClassName.OBJECT ),
						valueArrayName,
						paramName )
				.addStatement( "$T $L = { $L }",
						ArrayTypeName.of( TypeName.INT ),
						typeArrayName,
						primaryKey.sqlTypeNonenum )
				.addStatement( "$T $L = $L.deleteFrom( $L ).where( $L.$L.eq( $L ) ).getSQL()",
						String.class,
						deleteQueryName,
						jooqName,
						table.getName(),
						table.getName(),
						primaryKey.jooqName,
						paramName )
				.addStatement( "$L.update( $L, $L, $L )",
						jdbcTemplateName,
						deleteQueryName,
						valueArrayName,
						typeArrayName )
				.build();
		out.println( deleteById.toString() );
	}


	// unready
	private TypeName tnFromJooq(
			DataTypeDefinition typeOfColumn
	) {
		String nameOfSqlType = typeOfColumn.getType();
System.out.println( "-- dtd type is "+ nameOfSqlType );
		switch ( nameOfSqlType )
		{
			case "BOOLEAN" :
				return ClassName.BOOLEAN;
			case "INT" :
			case "INTEGER" :
				return ClassName.INT;
			case "DOUBLE" :
				return ClassName.DOUBLE;
			case "BIGINT" :
			case "IDENTITY" :
				return ClassName.LONG;
			case "TINYINT" :
				return ClassName.BYTE;
			case "SMALLINT" :
				return ClassName.SHORT;
			case "DECIMAL" :
				return ClassName.FLOAT; // FIX java.math.BigDecimal
			case "REAL" :
				return ClassName.FLOAT;
			case "CHAR" :
			case "VARCHAR" :
				return ClassName.get( String.class );
				/*
			case "TIME" :
				return TypeName.;
			case "TIME WITH TIME ZONE" :
				return TypeName.;
			case "DATE" :
				return TypeName.;
			case "TIMESTAMP" :
				return TypeName.;
			case "TIMESTAMP WITH TIME ZONE" :
				return TypeName.;
			case "BINARY" :
				return TypeName.;
			case "VARCHAR_IGNORECASE" :
				return TypeName.;
			case "BLOB" :
				return TypeName.;
			case "CLOB" :
				return TypeName.;
			case "UUID" :
				return TypeName.;
			case "ARRAY" :
				return TypeName.;
			case "ENUM" :
				return TypeName.;
			case "GEOMETRY" :
				return TypeName.;
			case "JSON" :
				return TypeName.;
			case "INTERVAL" :
				return TypeName.;
				*/
			case "OTHER" :
			default :
				return TypeName.OBJECT;
		}
	}


	// unready
	private String sqlTypeIntFromJooq(
			DataTypeDefinition typeOfColumn
	) {
		String nameOfSqlType = typeOfColumn.getType();
		switch ( nameOfSqlType )
		{
			case "BOOLEAN" :
				return "Types.BOOLEAN";
			case "INT" :
			case "INTEGER" :
				return "Types.INTEGER";
			case "DOUBLE" :
				return "Types.DOUBLE";
			case "BIGINT" :
			case "IDENTITY" :
				return "Types.BIGINT";
			case "TINYINT" :
				return "Types.TINYINT";
			case "SMALLINT" :
				return "Types.SMALLINT";
			case "DECIMAL" :
				return "Types.DECIMAL";
			case "REAL" :
				return "Types.REAL";
			case "CHAR" :
				return "Types.CHAR";
			case "VARCHAR" :
				return "Types.VARCHAR";
				/*
			case "TIME" :
				return TypeName.;
			case "TIME WITH TIME ZONE" :
				return TypeName.;
			case "DATE" :
				return TypeName.;
			case "TIMESTAMP" :
				return TypeName.;
			case "TIMESTAMP WITH TIME ZONE" :
				return TypeName.;
			case "BINARY" :
				return TypeName.;
			case "VARCHAR_IGNORECASE" :
				return TypeName.;
			case "CHAR" :
				return TypeName.;
			case "BLOB" :
				return TypeName.;
			case "CLOB" :
				return TypeName.;
			case "UUID" :
				return TypeName.;
			case "ARRAY" :
				return TypeName.;
			case "ENUM" :
				return TypeName.;
			case "GEOMETRY" :
				return TypeName.;
			case "JSON" :
				return TypeName.;
			case "INTERVAL" :
				return TypeName.;
				*/
			case "OTHER" :
			default :
				return "unrecognized-sqlTypeInt";
		}
	}


	// maybe okay
	class ColumnDef
	{
		TypeName jtype;
		String jooqName;
		String sqlTypeNonenum;

		public String toString()
		{
			return "tn:"+ jtype +" name:"+ jooqName +" sqitype:"+ sqlTypeNonenum;
		}
	}


}


















