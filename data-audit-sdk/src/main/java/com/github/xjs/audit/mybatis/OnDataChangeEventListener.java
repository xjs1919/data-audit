package com.github.xjs.audit.mybatis;

import com.github.xjs.audit.mybatis.dto.ChangeRowData;

import java.util.List;

public interface OnDataChangeEventListener {

	/**插入数据回调
	 * @param changeRows 变化数据
	 * */
	void onInsert(List<ChangeRowData> changeRows) ;

	/**修改数据回调
	 * @param changeRows 变化数据
	 * */
	void onUpdate(List<ChangeRowData> changeRows);

	/**删除数据回调
	 * @param changeRows 变化数据
	 * */
	void onDelete(List<ChangeRowData> changeRows);

}
