/* see ../../../../../LICENSE for release details */
package ws.nzen.format.jq.tkg;

import org.jooq.codegen.JavaGenerator;
import org.jooq.codegen.JavaWriter;
import org.jooq.meta.ColumnDefinition;
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


	protected void generatePojoClassFooter(
			TableDefinition table, JavaWriter out
	) {
		out.println( "\t// table name is "+ table.getName() );
		for ( ColumnDefinition column : table.getColumns() )
		{
			out.println( "\t// a column is "+ column.getName() +" sqltype "+ column.getType().getType() +" javatype "+ column.getType().getJavaType() +" identity? "+ column.isIdentity() );
		}
		for ( ColumnDefinition column : table.getPrimaryKey().getKeyColumns() )
		{
			out.println( "\t// a pk is "+ column.getName() +" sqltype "+ column.getType().getType() +" javatype "+ column.getType().getJavaType() +" identity? "+ column.isIdentity() );
		}
	}

}


















